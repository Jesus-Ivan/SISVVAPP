package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.CajaActivaDao
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class CajaRepository(
    private val api: ApiService,
    private val cajaActivaDao: CajaActivaDao
) {
    fun getCajaActiva(): Flow<CajaActivaEntity?> = cajaActivaDao.getCajaActiva()
    fun getCajasAbiertas(): Flow<List<CajaActivaEntity>> = cajaActivaDao.getCajasAbiertas()
    suspend fun sync(): Result<Unit> = runCatching {
        val response = api.getCajasActivas()
        if (response.isSuccessful) {
            val listaCajas = response.body() ?: emptyList()
            listaCajas.forEach { cajaDto ->
                cajaActivaDao.insertCajaActiva(cajaDto.toCajaActivaEntity())
            }
            Log.d("CajaRepo", "Cajas sincronizadas correctamente: ${listaCajas.size}")
        } else {
            val errorMsg = "Error sync cajas: HTTP ${response.code()} - ${response.message()}"
            Log.w("CajaRepo", errorMsg)
            throw Exception(errorMsg)
        }
    }
}
