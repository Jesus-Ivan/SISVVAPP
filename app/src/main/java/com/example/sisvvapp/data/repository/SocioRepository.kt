package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.SocioDao
import com.example.sisvvapp.data.local.dao.SocioWithIntegrantes
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class SocioRepository(
    private val api: ApiService,
    private val socioDao: SocioDao
) {

    fun getSocios(): Flow<List<SocioEntity>> = socioDao.getAllSocios()

    fun searchSocios(query: String): Flow<List<SocioEntity>> = socioDao.searchSocios(query)

    suspend fun getSocioConIntegrantes(socioId: Int): SocioWithIntegrantes? =
        socioDao.getSocioConIntegrantes(socioId)

    suspend fun sync(): Result<Unit> {
        return try {
            val response = api.getSocios()
            if (response.isSuccessful) {
                val sociosDto = response.body().orEmpty()
                val sociosEnt = sociosDto.map { it.toSocioEntity() }
                val integrantesEnt = sociosDto.flatMap { it.toIntegranteEntities() }

                socioDao.insertAllSocios(sociosEnt)
                socioDao.insertAllIntegrantes(integrantesEnt)

                Log.d("SocioRepo", "Sincronizados ${sociosEnt.size} socios")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin detalle"
                Log.w("SocioRepo", "Error sync socios: ${response.code()} — $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("SocioRepo", "Excepción en sync socios", e)
            Result.failure(e)
        }
    }
}
