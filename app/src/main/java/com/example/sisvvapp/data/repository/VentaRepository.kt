package com.example.sisvvapp.data.repository
import android.util.Log
import com.example.sisvvapp.data.local.dao.VentaColaDao
import com.example.sisvvapp.data.local.dao.VentaRecibidaDao
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.data.local.entity.VentaRecibidaEntity
import com.example.sisvvapp.network.ApiService
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.network.dto.ventas.VentaRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class VentaRepository(
    private val api: ApiService,
    private val ventaColaDao: VentaColaDao,
    private val ventaRecibidaDao: VentaRecibidaDao
) {
    private val gson = Gson()
    fun getPendientesCountFlow(): Flow<Int> = ventaColaDao.countPendientesFlow()
    suspend fun encolarVenta(venta: VentaColaEntity) {
        ventaColaDao.insert(venta)
    }
    suspend fun getPendientes(): List<VentaColaEntity> = ventaColaDao.getPendientes()
    fun getVentasRecibidas(corteCaja: Int): Flow<List<VentaDto>> {
        return ventaRecibidaDao.getVentasPorCorte(corteCaja).map { entities ->
            entities.map { it.toVentaDto() }
        }
    }
    fun getAllVentasRecibidas(): Flow<List<VentaDto>> {
        return ventaRecibidaDao.getAllVentas().map { entities ->
            entities.map { it.toVentaDto() }
        }
    }
    suspend fun getVentas(fecha: String, corteCaja: Int? = null): List<VentaDto> {
        return try {
            val response = api.getVentas(fecha, corteCaja)
            if (response.isSuccessful) {
                val ventas = response.body()?.map { it.toVentaDto() } ?: emptyList()
                // Guardar en Room para offline
                val entities = response.body()?.map { it.toVentaRecibidaEntity() } ?: emptyList()
                if (entities.isNotEmpty()) {
                    ventaRecibidaDao.insertAll(entities)
                }
                ventas
            } else {
                Log.w("VentaRepo", "Error al obtener ventas: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.w("VentaRepo", "Sin conexión, cargando ventas de Room", e)
            // Fallback: leer de Room
            emptyList()
        }
    }
    suspend fun syncVentas(fecha: String, corteCaja: Int? = null): Result<Unit> {
        return try {
            val response = api.getVentas(fecha, corteCaja)
            if (response.isSuccessful) {
                val entities = response.body()?.map { it.toVentaRecibidaEntity() } ?: emptyList()
                ventaRecibidaDao.deleteAll()
                if (entities.isNotEmpty()) {
                    ventaRecibidaDao.insertAll(entities)
                }
                Log.d("VentaRepo", "Ventas sincronizadas: ${entities.size}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.w("VentaRepo", "No hay conexión para sync ventas", e)
            Result.failure(e)
        }
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
private fun com.example.sisvvapp.network.dto.ventas.VentaResponse.toVentaRecibidaEntity(): VentaRecibidaEntity {
    val gson = Gson()
    val totalValue = when (total) {
        is Number -> (total as Number).toDouble()
        is String -> (total as String).toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
    val estatus = if (fechaCierre.isNullOrBlank()) "Abierta" else "Cerrada"
    return VentaRecibidaEntity(
        folio = folio,
        fecha = fechaApertura ?: "",
        total = totalValue,
        estado = estatus,
        clienteNombre = nombre,
        socioId = idSocio,
        clavePuntoVenta = clavePuntoVenta ?: "",
        corteCaja = corteCaja ?: 0,
        productosJson = "[]"
    )
}
private fun VentaRecibidaEntity.toVentaDto(): VentaDto {
    val (date, time) = fecha.split(" ").let {
        Pair(it.getOrElse(0) { "" }, it.getOrElse(1) { "" }.take(5))
    }
    return VentaDto(
        folio = folio,
        nombreCliente = clienteNombre ?: "N/A",
        hora = time,
        total = total,
        estatus = estado,
        cajaId = corteCaja,
        socioId = socioId,
        tipoCliente = null,
        fecha = date
    )
}