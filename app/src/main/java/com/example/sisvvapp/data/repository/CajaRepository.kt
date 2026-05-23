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

    // Función para consultar UNA caja
    fun getCajaActiva(): Flow<CajaActivaEntity?> = cajaActivaDao.getCajaActiva()

    // Función que devuelve el flujo de TODAS las cajas para la lista
    fun getCajasAbiertas(): Flow<List<CajaActivaEntity>> = cajaActivaDao.getCajasAbiertas()

    suspend fun sync(): Result<Unit> = runCatching {
        // Llamamos al endpoint que ahora devuelve una lista de cajas
        val response = api.getCajasActivas()

        if (response.isSuccessful) {
            // Extraemos la lista, y si viene nula, usamos una lista vacía
            val listaCajas = response.body() ?: emptyList()

            // Iteramos sobre cada caja de la respuesta para insertarla en Room
            listaCajas.forEach { cajaDto ->
                cajaActivaDao.insertCajaActiva(cajaDto.toCajaActivaEntity())
            }

            Log.d("CajaRepo", "Cajas sincronizadas correctamente: ${listaCajas.size}")
        } else {
            Log.w("CajaRepo", "Error sync cajas: ${response.code()}")
        }
    }
}