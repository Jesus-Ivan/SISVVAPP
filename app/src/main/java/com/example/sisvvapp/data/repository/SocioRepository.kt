package com.example.sisvvapp.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.dao.SocioDao
import com.example.sisvvapp.data.local.dao.SocioWithIntegrantes
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.sync.PhotoDownloader
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class SocioRepository(
    private val api: ApiService,
    private val db: AppDatabase,
    private val socioDao: SocioDao,
    private val context: Context? = null
) {

    fun getSocios(): Flow<List<SocioEntity>> = socioDao.getAllSocios()

    fun searchSocios(query: String): Flow<List<SocioEntity>> = socioDao.searchSocios(query)

    suspend fun getSocioConIntegrantes(socioId: Int): SocioWithIntegrantes? =
        socioDao.getSocioConIntegrantes(socioId)

    suspend fun getSocioById(id: Int): SocioEntity? = socioDao.getSocioById(id)

    suspend fun sync(): Result<Unit> {
        return try {
            val response = api.getSocios()
            if (response.isSuccessful) {
                val sociosDto = response.body().orEmpty()
                val sociosEnt = sociosDto.map { it.toSocioEntity() }
                val integrantesEnt = sociosDto.flatMap { it.toIntegranteEntities() }
                db.withTransaction {
                    socioDao.deleteAll()
                    socioDao.insertAllSocios(sociosEnt)
                    socioDao.insertAllIntegrantes(integrantesEnt)
                }
                Log.d("SocioRepo", "Sincronizados ${sociosEnt.size} socios")

                // La descarga de fotos ya no debe bloquear el sync de datos
                // El SyncWorker se encargará de bajarlas gradualmente en segundo plano
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
