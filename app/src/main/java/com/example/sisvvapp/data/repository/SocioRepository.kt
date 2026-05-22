package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.SocioDao
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class SocioRepository(
    private val api: ApiService,
    private val socioDao: SocioDao
) {

    fun getSocios(): Flow<List<SocioEntity>> = socioDao.getAllSocios()

    fun searchSocios(query: String): Flow<List<SocioEntity>> = socioDao.searchSocios(query)

    suspend fun sync(): Result<Unit> = runCatching {
        val response = api.getSocios()
        if (response.isSuccessful) {
            val sociosDto = response.body().orEmpty()
            val sociosEnt = sociosDto.map { it.toSocioEntity() }
            val integrantesEnt = sociosDto.flatMap { it.toIntegranteEntities() }

            socioDao.insertAllSocios(sociosEnt)
            socioDao.insertAllIntegrantes(integrantesEnt)

            Log.d("SocioRepo", "Sincronizados ${sociosEnt.size} socios")
        } else {
            Log.w("SocioRepo", "Error sync socios: ${response.code()}")
        }
    }
}
