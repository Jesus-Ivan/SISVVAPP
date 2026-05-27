package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.VentaColaDao
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.network.ApiService
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.network.dto.ventas.VentaRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class VentaRepository(
    private val api: ApiService,
    private val ventaColaDao: VentaColaDao
) {
    private val gson = Gson()

    fun getPendientesCountFlow(): Flow<Int> = ventaColaDao.countPendientesFlow()

    suspend fun encolarVenta(venta: VentaColaEntity) {
        ventaColaDao.insert(venta)
    }

    suspend fun getPendientes(): List<VentaColaEntity> = ventaColaDao.getPendientes()

    suspend fun getVentas(fecha: String, corteCaja: Int? = null): List<VentaDto> {
        val response = api.getVentas(fecha, corteCaja)
        if (response.isSuccessful) {
            return response.body()?.map { it.toVentaDto() } ?: emptyList()
        }
        Log.w("VentaRepo", "Error al obtener ventas: ${response.code()}")
        return emptyList()
    }

    suspend fun enviarVentaOffline(venta: VentaColaEntity): Result<Unit> = runCatching {
        val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
        val productos: List<ItemCarritoDto> = gson.fromJson(venta.productosJson, type)

        val request = VentaRequest(
            corteCaja = venta.corteCaja,
            tipoVenta = venta.tipoVenta,
            idSocio = venta.idSocio,
            nombre = venta.nombreCliente,
            clavePuntoVenta = venta.clavePuntoVenta,
            productos = productos
        )

        val response = api.crearVenta(request)

        if (response.isSuccessful) {
            ventaColaDao.deleteById(venta.idTemporal)
            Log.d("VentaRepo", "Venta ${venta.idTemporal} sincronizada con éxito")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error ${response.code()}: $errorBody")
        }
    }
}
