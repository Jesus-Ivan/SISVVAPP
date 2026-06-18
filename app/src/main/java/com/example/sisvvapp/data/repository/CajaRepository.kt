package com.example.sisvvapp.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.dao.CajaActivaDao
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class CajaRepository(
    private val api: ApiService,
    private val db: AppDatabase,
    private val cajaActivaDao: CajaActivaDao
) {
    fun getCajaActiva(): Flow<CajaActivaEntity?> = cajaActivaDao.getCajaActiva()
    fun getCajasAbiertas(): Flow<List<CajaActivaEntity>> = cajaActivaDao.getCajasAbiertas()
    // One-shot: devuelve la lista actual de Room justo después de un sync
    suspend fun getCajasSnapshot(): List<CajaActivaEntity> = cajaActivaDao.getCajasSnapshot()
    suspend fun sync(): Result<Unit> {
        return try {
            val response = api.getCajasActivas()
            if (response.isSuccessful) {
                val listaCajas = response.body() ?: emptyList()
                db.withTransaction {
                    cajaActivaDao.deleteAll()
                    listaCajas.forEach { cajaDto ->
                        cajaActivaDao.insertCajaActiva(cajaDto.toCajaActivaEntity())
                    }
                }
                Log.d("CajaRepo", "Cajas sincronizadas correctamente: ${listaCajas.size}")
            } else {
                val errorMsg = "Error sync cajas: HTTP ${response.code()} - ${response.message()}"
                Log.w("CajaRepo", errorMsg)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CajaRepo", "Excepción en sync cajas", e)
            Result.failure(e)
        }
    }
}
