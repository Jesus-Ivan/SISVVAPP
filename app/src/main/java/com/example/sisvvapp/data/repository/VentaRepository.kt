package com.example.sisvvapp.data.repository
import android.util.Log
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.data.local.entity.VentaRecibidaEntity
import com.example.sisvvapp.data.local.view.VentaGlobalView
import androidx.room.withTransaction
import com.example.sisvvapp.network.ApiService
import com.example.sisvvapp.network.ApiResult
import com.example.sisvvapp.network.exceptions.ServerUnreachableException
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto
import com.example.sisvvapp.network.dto.ventas.PagoDto
import com.example.sisvvapp.network.dto.ventas.ProductoVentaDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.network.dto.ventas.TransferirProductoRequest
import com.example.sisvvapp.network.dto.ventas.VentaRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class VentaRepository(
    private val api: ApiService,
    private val db: AppDatabase,
) {
    private val ventaColaDao = db.ventaColaDao()
    private val ventaRecibidaDao = db.ventaRecibidaDao()
    private val ventaGlobalDao = db.ventaGlobalDao()

    private val gson = Gson()
    fun getPendientesCountFlow(): Flow<Int> = ventaColaDao.countPendientesFlow()
    suspend fun getPendientes(): List<VentaColaEntity> = ventaColaDao.getPendientes()
    fun getVentasGlobales(corteCaja: Int? = null, fecha: String): Flow<List<VentaDto>> {
        val flow = if (corteCaja != null) {
            ventaGlobalDao.getVentasPorCorte(corteCaja, fecha)
        } else {
            ventaGlobalDao.getVentasGlobales(fecha)
        }
        return flow.map { list -> list.map { it.toVentaDto() } }
    }

    fun getVentasRecibidas(corteCaja: Int): Flow<List<VentaDto>> {
        return ventaRecibidaDao.getVentasPorCorte(corteCaja).map { entities ->
            entities.map { it.toVentaDto() }
        }
    }
    suspend fun getVentaDetalleGlobal(id: String): VentaDto? {
        val folio = id.toIntOrNull()
        val localCola = ventaColaDao.getById(id)
        
        return if (localCola != null) {
            // Caso 1: Es una venta en la cola (Nueva offline o Append offline)
            val dto = localCola.toVentaDto()
            if (localCola.folioExistente != null && (localCola.folioExistente > 0)) {
                // Es un append offline, necesitamos combinar con los productos ya existentes en el servidor/histórico
                val baseVenta = getVentaDetalle(localCola.folioExistente)
                if (baseVenta != null) {
                    // Combinamos y agrupamos para evitar duplicados visuales (ej. 2 Tacos + 1 Taco -> 3 Tacos)
                    // IMPORTANTE: Agrupamos también por idEstado para que lo ya impreso no se mezcle con lo nuevo pendiente
                    val combinedProductos = (baseVenta.productos + dto.productos)
                        .groupBy { it.claveProducto to it.observaciones to (it.idEstado ?: "") }
                        .map { (_, list) ->
                            val first = list.first()
                            first.copy(
                                cantidad = list.sumOf { it.cantidad },
                                subtotal = list.sumOf { it.subtotal }
                            )
                        }
                    dto.copy(
                        productos = combinedProductos,
                        pagos = baseVenta.pagos,
                        total = baseVenta.total + dto.total,
                    )
                } else {
                    dto
                }
            } else {
                dto
            }
        } else if (folio != null && folio > 0) {
            // Caso 2: Es una venta ya recibida normal
            getVentaDetalle(folio)
        } else {
            null
        }
    }

    suspend fun getVentaDetalle(folio: Int): VentaDto? {
        return try {
            val response = api.getVentaDetalle(folio)
            if (response.isSuccessful) {
                val dto = response.body()?.toVentaDto()
                if (dto != null) {
                    // Actualizamos caché local por si acaso
                    db.withTransaction {
                        ventaRecibidaDao.insertAll(listOf(response.body()!!.toVentaRecibidaEntity()))
                    }
                }
                dto
            } else {
                Log.w("VentaRepo", "Error ${response.code()} al obtener detalle, usando local")
                ventaRecibidaDao.getVentaPorFolio(folio)?.toVentaDto()
            }
        } catch (e: Exception) {
            Log.w("VentaRepo", "Offline, usando datos locales para detalle", e)
            ventaRecibidaDao.getVentaPorFolio(folio)?.toVentaDto()
        }
    }

    suspend fun syncVentas(fecha: String, corteCaja: Int? = null): ApiResult<Unit> {
        return try {
            val response = api.getVentas(fecha, corteCaja)
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                
                // 1. Preservamos productos locales si el servidor envía lista vacía
                val updatedEntities = body.map { remote ->
                    val remoteEntity = remote.toVentaRecibidaEntity()
                    val localEntity = ventaRecibidaDao.getVentaPorFolio(remote.folio)
                    
                    if (localEntity != null && remoteEntity.productosJson == "[]" && localEntity.productosJson != "[]") {
                        remoteEntity.copy(productosJson = localEntity.productosJson)
                    } else {
                        remoteEntity
                    }
                }
                
                ventaRecibidaDao.insertAll(updatedEntities)
                Log.d("VentaRepo", "Ventas sincronizadas para $fecha: ${updatedEntities.size}")

                // 2. PREFETCH: Descargar detalles de ventas ABIERTAS que no tengan productos
                val abiertasSinDetalle = updatedEntities.filter { 
                    it.estado.equals("Abierta", ignoreCase = true) && it.productosJson == "[]"
                }
                
                if (abiertasSinDetalle.isNotEmpty()) {
                    Log.d("VentaRepo", "Iniciando prefetch para ${abiertasSinDetalle.size} ventas")
                    abiertasSinDetalle.forEach { entity ->
                        try {
                            getVentaDetalle(entity.folio)
                        } catch (_: Exception) {
                            Log.w("VentaRepo", "Fallo prefetch folio ${entity.folio}")
                        }
                    }
                }

                ApiResult.Success(Unit)
            } else {
                ApiResult.ServerError(response.code(), response.message())
            }
        } catch (e: ServerUnreachableException) {
            Log.w("VentaRepo", "Servidor inalcanzable: ${e.message}")
            ApiResult.NetworkError("Servidor fuera de línea")
        } catch (e: Exception) {
            Log.w("VentaRepo", "Error de red al sync ventas", e)
            ApiResult.NetworkError("Error de conexión")
        }
    }
    suspend fun crearVenta(request: VentaRequest): Result<com.example.sisvvapp.network.dto.ventas.VentaResponse> {
        return try {
            val response = api.crearVenta(request)
            if (response.isSuccessful) {
                val body = response.body()!!
                Log.d("VentaRepo", "Venta creada: folio ${body.folio}")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("VentaRepo", "Error al crear venta: ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val isNetworkError = msg.contains("unable to resolve host") ||
                    msg.contains("timeout") ||
                    msg.contains("failed to connect") ||
                    msg.contains("network is unreachable") ||
                    msg.contains("route to host")
            if (isNetworkError) {
                Log.w("VentaRepo", "Sin conexión, encolando venta offline", e)
                encolarVentaOffline(request, null)
                Result.failure(Exception("offline"))
            } else {
                Log.e("VentaRepo", "Error no recuperable al crear venta, no se encola", e)
                Result.failure(Exception("Error de comunicación: ${e.message}"))
            }
        }
    }

    suspend fun appendProductos(folio: Int, request: VentaRequest, idTemporal: String? = null): Result<Unit> {
        return try {
            val response = api.appendProductos(folio, request)
            if (response.isSuccessful) {
                Log.d("VentaRepo", "Productos agregados a venta $folio")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("VentaRepo", "Error al agregar productos: ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val isNetworkError = msg.contains("unable to resolve host") ||
                    msg.contains("timeout") ||
                    msg.contains("failed to connect") ||
                    msg.contains("network is unreachable") ||
                    msg.contains("route to host")
            if (isNetworkError) {
                Log.w("VentaRepo", "Sin conexión, encolando append offline")
                encolarVentaOffline(request, if (folio > 0) folio else null, idTemporal)
                Result.failure(Exception("offline"))
            } else {
                Log.e("VentaRepo", "Error no recuperable al agregar productos, no se encola", e)
                Result.failure(Exception("Error de comunicación: ${e.message}"))
            }
        }
    }

    private suspend fun encolarVentaOffline(request: VentaRequest, folioExistente: Int?, idTemporalExistente: String? = null) {
        val finalIdTemporal = idTemporalExistente ?: java.util.UUID.randomUUID().toString()
        
        // Buscamos si ya existe en la cola para no duplicar si es una edición
        val existente = if (folioExistente != null && folioExistente > 0) {
            ventaColaDao.getByFolioExistente(folioExistente)
        } else if (idTemporalExistente != null) {
            ventaColaDao.getById(idTemporalExistente)
        } else {
            null
        }

        val entity = if (existente != null) {
            // MERGE: Si ya existe, combinamos los productos
            val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
            val productosExistentes: List<ItemCarritoDto> = gson.fromJson(existente.productosJson, type)
            val nuevosProductos = request.productos
            
            // Combinar y agrupar para evitar filas separadas del mismo producto
            val listaCombinada = (productosExistentes + nuevosProductos)
                .groupBy { it.claveProducto to it.observaciones to it.modificadores }
                .map { (_, list) ->
                    list.first().copy(cantidad = list.sumOf { it.cantidad })
                }
            
            existente.copy(
                productosJson = gson.toJson(listaCombinada),
                totalVenta = listaCombinada.sumOf { (it.cantidad * (it.precio ?: 0.0)) + it.modificadores.sumOf { m -> m.cantidad * (m.precio ?: 0.0) } },
                fechaCreacion = System.currentTimeMillis(),
                estado = "PENDIENTE"
            )
        } else {
            VentaColaEntity(
                idTemporal = finalIdTemporal,
                tipoVenta = request.tipoVenta,
                idSocio = request.idSocio,
                nombreCliente = request.nombre ?: "",
                corteCaja = request.corteCaja,
                clavePuntoVenta = request.clavePuntoVenta,
                nombreCaja = "",
                productosJson = gson.toJson(request.productos),
                fechaCreacion = System.currentTimeMillis(),
                totalVenta = request.total ?: 0.0,
                estado = "PENDIENTE",
                folioExistente = folioExistente,
                pagosJson = null
            )
        }
        ventaColaDao.insert(entity)
    }

    suspend fun enviarVentaOffline(venta: VentaColaEntity): Result<Unit> = runCatching {
        // 1. Marcar como SYNCING
        ventaColaDao.updateEstado(venta.idTemporal, "SYNCING")

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
        
        try {
            val response = if (venta.folioExistente != null) {
                api.appendProductos(venta.folioExistente, request)
            } else {
                api.crearVenta(request)
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    db.withTransaction {
                        // 1. Invalidamos caché local para evitar inconsistencias visuales
                        ventaRecibidaDao.deleteByFolio(venta.folioExistente ?: 0)
                        // 2. Guardamos la respuesta fresca del servidor (con folios y estados finales)
                        ventaRecibidaDao.insertAll(listOf(body.toVentaRecibidaEntity()))
                        // 3. Limpiamos la cola local
                        ventaColaDao.deleteById(venta.idTemporal)
                    }
                    // 4. Forzamos descarga del detalle para actualizar estados de impresión
                    if (venta.folioExistente != null && venta.folioExistente > 0) {
                        getVentaDetalle(venta.folioExistente)
                    }
                    // 5. Sincronizamos la lista general
                    val fechaStr = java.time.LocalDate.now().toString()
                    syncVentas(fechaStr, venta.corteCaja)
                } else {
                    ventaColaDao.deleteById(venta.idTemporal)
                }
                Log.d("VentaRepo", "Venta ${venta.idTemporal} sincronizada con éxito")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                ventaColaDao.updateEstado(venta.idTemporal, "ERROR")
                throw Exception("Error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            ventaColaDao.updateEstado(venta.idTemporal, "ERROR")
            throw e
        }
    }

    suspend fun procesarColaVentas(): Int {
        val cola = ventaColaDao.getParaSincronizar()
        if (cola.isEmpty()) return 0

        var sincronizadasExitosamente = 0

        for (venta in cola) {
            try {
                val resultado = enviarVentaOffline(venta)
                if (resultado.isSuccess) {
                    sincronizadasExitosamente++
                } else {
                    Log.w("VentaRepo", "Fallo al sincronizar venta ${venta.idTemporal}, deteniendo cola.")
                    break
                }
            } catch (e: Exception) {
                Log.e("VentaRepo", "Error inesperado en sincronización", e)
                break
            }
        }
        return sincronizadasExitosamente
    }

    suspend fun getVentaRecibidaPorFolio(folio: Int): VentaRecibidaEntity? {
        return ventaRecibidaDao.getVentaPorFolio(folio)
    }

    suspend fun transferirProducto(
        folioOrigen: Int,
        chunk: Long,
        folioDestino: Int
    ): Result<Unit> {
        return try {
            val response = api.transferirProducto(
                folioOrigen,
                TransferirProductoRequest(folioDestino, chunk)
            )
            if (response.isSuccessful) {
                Log.d("VentaRepo", "Producto transferido: chunk $chunk de $folioOrigen a $folioDestino")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("VentaRepo", "Error transferencia: ${response.code()} - $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e("VentaRepo", "Error de red al transferir", e)
            Result.failure(Exception("Se requiere conexión para transferir productos"))
        }
    }

    suspend fun reimprimirComanda(folio: Int): Result<Unit> {
        return try {
            val response = api.reimprimirComanda(folio)
            if (response.isSuccessful) {
                Log.d("VentaRepo", "Reimpresión solicitada para folio $folio")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("VentaRepo", "Error reimpresión: ${response.code()} - $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e("VentaRepo", "Error de red al reimprimir", e)
            Result.failure(Exception("Se requiere conexión para reimprimir"))
        }
    }
}
private fun VentaColaEntity.toVentaDto(): VentaDto {
    val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    val displayDate = sdfDate.format(java.util.Date(fechaCreacion))
    val displayTime = sdfTime.format(java.util.Date(fechaCreacion))

    val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
    val items: List<ItemCarritoDto> = try {
        Gson().fromJson(productosJson, type)
    } catch (e: Exception) {
        emptyList()
    }

    val productosVenta = items.map { item ->
        ProductoVentaDto(
            id = item.claveProducto,
            claveProducto = item.claveProducto,
            nombre = item.nombre ?: "Producto #${item.claveProducto}",
            precio = item.precio ?: 0.0,
            cantidad = item.cantidad,
            chunk = 0,
            observaciones = item.observaciones,
            subtotal = (item.precio ?: 0.0) * item.cantidad,
            idEstado = if (item.printDefault) "0" else "", // Forzamos estado 0 solo si imprime comanda
            modificadores = item.modificadores
        )
    }

    return VentaDto(
        folio = folioExistente ?: 0,
        nombreCliente = nombreCliente,
        hora = displayTime,
        total = totalVenta,
        estatus = "Abierta",
        cajaId = corteCaja,
        socioId = idSocio,
        tipoCliente = tipoVenta,
        fecha = displayDate,
        clavePuntoVenta = clavePuntoVenta,
        productos = productosVenta,
        syncStatus = estado,
        idTemporal = idTemporal
    )
}

private fun VentaGlobalView.toVentaDto(): VentaDto {
    val displayDate: String
    val displayTime: String

    if (folio == null || folio == 0 || syncStatus != "RECIBIDA") {
        // Es offline, fecha es un timestamp (Long) convertido a String en la vista
        val ts = fecha.toLongOrNull() ?: timestamp
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        displayDate = sdfDate.format(java.util.Date(ts))
        displayTime = sdfTime.format(java.util.Date(ts))
    } else {
        // Es recibida, fecha es "yyyy-MM-dd HH:mm:ss"
        val parts = fecha.split(" ")
        displayDate = parts.getOrElse(0) { "" }
        displayTime = parts.getOrElse(1) { "" }.take(5)
    }

    val gson = Gson()
    val productos: List<ProductoVentaDto> = try {
        if (syncStatus != "RECIBIDA") {
            // Reconstrucción para registros offline (nuevos o ediciones)
            val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
            val items: List<ItemCarritoDto> = gson.fromJson(productosJson, type)
            items.map { item ->
                ProductoVentaDto(
                    id = item.claveProducto,
                    claveProducto = item.claveProducto,
                    nombre = item.nombre ?: "Producto #${item.claveProducto}",
                    precio = item.precio ?: 0.0,
                    cantidad = item.cantidad,
                    chunk = 0,
                    observaciones = item.observaciones,
                    subtotal = (item.precio ?: 0.0) * item.cantidad,
                    idEstado = if (item.printDefault) "0" else "", // Forzamos estado 0 solo si imprime comanda
                    modificadores = item.modificadores
                )
            }
        } else {
            // Venta oficial del servidor
            gson.fromJson(productosJson, object : TypeToken<List<ProductoVentaDto>>() {}.type)
        }
    } catch (_: Exception) {
        emptyList()
    }

    return VentaDto(
        folio = folio ?: 0,
        nombreCliente = cliente ?: "N/A",
        hora = displayTime,
        total = total,
        estatus = estadoVenta,
        cajaId = corteCaja,
        socioId = if (socioId == 0) null else socioId,
        tipoCliente = null,
        fecha = displayDate,
        productos = productos,
        syncStatus = syncStatus,
        idTemporal = idTemporal
    )
}

private fun com.example.sisvvapp.network.dto.ventas.VentaResponse.toVentaRecibidaEntity(): VentaRecibidaEntity {
    val gson = Gson()
    val totalValue = when (total) {
        is Number -> total.toDouble()
        is String -> total.toDoubleOrNull() ?: 0.0
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
        productosJson = gson.toJson(productos ?: emptyList<ProductoVentaDto>()),
        pagosJson = gson.toJson(pagos ?: emptyList<PagoDto>())
    )
}
private fun VentaRecibidaEntity.toVentaDto(): VentaDto {
    val (date, time) = fecha.split(" ").let {
        Pair(it.getOrElse(0) { "" }, it.getOrElse(1) { "" }.take(5))
    }
    val gson = Gson()
    val productos: List<ProductoVentaDto> = try {
        gson.fromJson(productosJson, object : TypeToken<List<ProductoVentaDto>>() {}.type)
    } catch (_: Exception) {
        emptyList()
    }
    val pagos: List<PagoDto> = try {
        gson.fromJson(pagosJson, object : TypeToken<List<PagoDto>>() {}.type)
    } catch (_: Exception) {
        emptyList()
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
        fecha = date,
        productos = productos,
        pagos = pagos,
        syncStatus = "RECIBIDA"
    )
}
