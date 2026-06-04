package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.TipoVentaDao
import com.example.sisvvapp.data.local.entity.TipoVentaEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class TipoVentaRepository(
    private val api: ApiService,
    private val tipoVentaDao: TipoVentaDao
) {
    fun getTiposVentaFlow(): Flow<List<TipoVentaEntity>> = tipoVentaDao.getAllTiposVentaFlow()

    suspend fun sync(): Result<Unit> = runCatching {
        val response = api.getTiposVenta()
        if (response.isSuccessful) {
            val tiposStr = response.body().orEmpty()
            val entities = tiposStr.map { TipoVentaEntity(nombre = it) }
            tipoVentaDao.deleteAll()
            tipoVentaDao.insertAll(entities)
            Log.d("TipoVentaRepo", "Sincronizados ${entities.size} tipos de venta")
        } else {
            Log.w("TipoVentaRepo", "Error sync tipos de venta: ${response.code()}")
        }
    }
}
