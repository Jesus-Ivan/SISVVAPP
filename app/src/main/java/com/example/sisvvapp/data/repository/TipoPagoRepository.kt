package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.TipoPagoDao
import com.example.sisvvapp.data.local.entity.TipoPagoEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class TipoPagoRepository(
    private val api: ApiService,
    private val tipoPagoDao: TipoPagoDao
) {

    fun getTiposPago(): Flow<List<TipoPagoEntity>> = tipoPagoDao.getTiposPago()

    suspend fun sync(): Result<Unit> = runCatching {
        val response = api.getTiposPago()
        if (response.isSuccessful) {
            val tipos = response.body()?.map { dto ->
                TipoPagoEntity(
                    id = dto.id,
                    nombre = dto.nombre,
                    requiereSocio = dto.requiereSocio,
                    requiereFirma = dto.requiereFirma,
                    activo = dto.activo
                )
            } ?: emptyList()
            tipoPagoDao.deleteAll()
            tipoPagoDao.insertAll(tipos)
            Log.d("TipoPagoRepo", "Sincronizados ${tipos.size} tipos de pago")
        } else {
            throw Exception("Error sync tipos pago: HTTP ${response.code()}")
        }
    }
}
