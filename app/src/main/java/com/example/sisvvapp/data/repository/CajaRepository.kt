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

    suspend fun sync(): Result<Unit> = runCatching {
        val response = api.getCajaActiva()
        if (response.isSuccessful) {
            val cajaDto = response.body()
            if (cajaDto != null) {
                cajaActivaDao.insertCajaActiva(cajaDto.toCajaActivaEntity())
                Log.d("CajaRepo", "Caja activa sincronizada: ${cajaDto.nombre}")
            }
        } else {
            Log.w("CajaRepo", "Error sync caja: ${response.code()}")
        }
    }
}
