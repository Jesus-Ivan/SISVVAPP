package com.example.sisvvapp.data.repository
import android.util.Log
import android.content.Context
import com.example.sisvvapp.data.sync.SyncForegroundService
import com.example.sisvvapp.data.sync.SyncWorker
import com.example.sisvvapp.data.sync.WatchdogWorker
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class VentaRepository(
    private val api: ApiService,
    private val db: AppDatabase,
    private val context: Context
) {
    private val ventaColaDao = db.ventaColaDao()
    private val ventaRecibidaDao = db.ventaRecibidaDao()
    private val ventaGlobalDao = db.ventaGlobalDao()

    private val gson = Gson()
    fun getPendientesCountFlow(): Flow<Int> = ventaColaDao.countPendientesFlow()
    suspend fun getPendientes(): List<VentaColaEntity> = ventaColaDao.getPendientes()
    suspend fun getParaSincronizar(): List<VentaColaEntity> = ventaColaDao.getParaSincronizar()
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
    suspend fun crearVenta(request: VentaRequest, idTemporal: String): Result<com.example.sisvvapp.network.dto.ventas.VentaResponse> {
        val requestConId = request.copy(requestId = idTemporal)
        return try {
            val response = api.crearVenta(requestConId)
            if (response.isSuccessful) {
                val body = response.body()!!
                Log.d("VentaRepo", "Venta creada: folio ${body.folio}")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("VentaRepo", "Error al crear venta: ${response.code()} - $errorBody")
                if (response.code() >= 500 || response.code() == 429) {
                    encolarVentaOffline(request, null, idTemporal)
                    Result.failure(Exception("offline"))
                } else {
                    Result.failure(Exception("Error ${response.code()}: $errorBody"))
                }
            }
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val isNetworkError = e is ServerUnreachableException ||
                    msg.contains("unable to resolve host") ||
                    msg.contains("timeout") ||
                    msg.contains("failed to connect") ||
                    msg.contains("network is unreachable") ||
                    msg.contains("route to host") ||
                    msg.contains("connection refused")
            
            if (isNetworkError) {
                Log.w("VentaRepo", "Sin conexión o servidor inalcanzable, encolando venta offline", e)
                encolarVentaOffline(request, null, idTemporal)
                Result.failure(Exception("offline"))
            } else {
                Log.e("VentaRepo", "Error no recuperable al crear venta, no se encola", e)
                Result.failure(Exception("Error de comunicación: ${e.message}"))
            }
        }
    }

    suspend fun appendProductos(folio: Int, request: VentaRequest, idTemporal: String? = null): Result<Unit> {
        val requestConId = if (idTemporal != null) request.copy(requestId = idTemporal) else request
        return try {
            val response = api.appendProductos(folio, requestConId)
            if (response.isSuccessful) {
                Log.d("VentaRepo", "Productos agregados a venta $folio")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("VentaRepo", "Error al agregar productos: ${response.code()} - $errorBody")
                if (response.code() >= 500 || response.code() == 429) {
                    encolarVentaOffline(request, if (folio > 0) folio else null, idTemporal)
                    Result.failure(Exception("offline"))
                } else {
                    Result.failure(Exception("Error ${response.code()}: $errorBody"))
                }
            }
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val isNetworkError = e is ServerUnreachableException ||
                    msg.contains("unable to resolve host") ||
                    msg.contains("timeout") ||
                    msg.contains("failed to connect") ||
                    msg.contains("network is unreachable") ||
                    msg.contains("route to host") ||
                    msg.contains("connection refused")
            
            if (isNetworkError) {
                Log.w("VentaRepo", "Sin conexión o servidor inalcanzable, encolando append offline")
                encolarVentaOffline(request, if (folio > 0) folio else null, idTemporal)
                Result.failure(Exception("offline"))
            } else {
                Log.e("VentaRepo", "Error no recuperable al agregar productos, no se encola", e)
                Result.failure(Exception("Error de comunicación: ${e.message}"))
            }
        }
    }

    suspend fun encolarVentaOffline(request: VentaRequest, folioExistente: Int?, idTemporalExistente: String? = null) {
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
        
        // DISPARADOR PROACTIVO: Pedimos a Android que intente sincronizar en cuanto haya red
        SyncWorker.enqueueOneTime(context)

        // Asegurar que el foreground service esté corriendo
        SyncForegroundService.start(context)

        // Watchdog para monitoreo post-force-kill
        WatchdogWorker.enqueue(context)
    }

    suspend fun enviarVentaOffline(venta: VentaColaEntity): Result<Unit> = runCatching {
        // Si el registro estaba atascado en SYNCING (proceso matado), reseteamos a PENDIENTE
        if (venta.estado == "SYNCING") {
            ventaColaDao.updateEstado(venta.idTemporal, "PENDIENTE")
        }
        // 1. Marcar como SYNCING
        ventaColaDao.updateEstado(venta.idTemporal, "SYNCING")

        // Re-leer de DB para obtener datos actualizados (posible merge local)
        val actual = ventaColaDao.getById(venta.idTemporal) ?: return Result.success(Unit)
        val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
        val productos: List<ItemCarritoDto> = try {
            gson.fromJson(actual.productosJson, type)
        } catch (e: Exception) {
            emptyList()
        }
        val request = VentaRequest(
            requestId = venta.idTemporal,
            corteCaja = actual.corteCaja,
            tipoVenta = actual.tipoVenta,
            idSocio = actual.idSocio,
            nombre = actual.nombreCliente,
            clavePuntoVenta = actual.clavePuntoVenta,
            productos = productos
        )

        var lastError: Exception? = null
        val maxRetries = 3

        for (attempt in 1..maxRetries) {
            try {
                val response = if (actual.folioExistente != null && actual.folioExistente > 0) {
                    api.appendProductos(actual.folioExistente, request)
                } else {
                    api.crearVenta(request)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (venta.folioExistente != null && venta.folioExistente > 0) {
                            // APPEND: response solo tiene {folio} — descargar datos completos
                            val detalle = getVentaDetalle(venta.folioExistente)
                            if (detalle != null) {
                                db.withTransaction {
                                    ventaRecibidaDao.deleteByFolio(venta.folioExistente)
                                    ventaRecibidaDao.insertAll(listOf(detalle.toVentaRecibidaEntity()))
                                    ventaColaDao.deleteById(venta.idTemporal)
                                }
                            } else {
                                // No se pudo obtener detalle, pero append ya se aplicó en servidor
                                // Mantener caché local intacta, solo remover de cola
                                ventaColaDao.deleteById(venta.idTemporal)
                            }
                        } else {
                            // NUEVA VENTA: insertar response y luego refrescar con detalles completos
                            db.withTransaction {
                                ventaRecibidaDao.deleteByFolio(0) // no-op para folio=0
                                ventaRecibidaDao.insertAll(listOf(body.toVentaRecibidaEntity()))
                                ventaColaDao.deleteById(venta.idTemporal)
                            }
                            getVentaDetalle(body.folio) // safeguard para poblar datos completos
                        }
                        val fechaStr = java.time.LocalDate.now().toString()
                        syncVentas(fechaStr, venta.corteCaja)
                    } else {
                        // Éxito sin body — remover de cola, el servidor tiene los datos
                        ventaColaDao.deleteById(venta.idTemporal)
                    }
                    Log.d("VentaRepo", "Venta ${venta.idTemporal} Sincronizada con éxito")
                    return Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("VentaRepo", "Error ${response.code()} al sincronizar venta ${venta.idTemporal}: $errorBody")
                    
                    if (response.code() == 409) {
                        Log.d("VentaRepo", "Venta ${venta.idTemporal} ya existe en servidor, eliminando de cola")
                        ventaColaDao.deleteById(venta.idTemporal)
                        return Result.success(Unit)
                    }
                    
                    if (response.code() in 400..499 && response.code() != 429) {
                        Log.w("VentaRepo", "Error de cliente ${response.code()}, marcando como ERROR_FATAL")
                        ventaColaDao.updateEstado(venta.idTemporal, "ERROR_FATAL")
                        throw Exception("Error ${response.code()}: $errorBody")
                    }
                    
                    lastError = Exception("Error ${response.code()}: $errorBody")
                    Log.w("VentaRepo", "Intento $attempt/$maxRetries falló (${response.code()}), reintentando...")
                }
            } catch (e: Exception) {
                lastError = e
                Log.w("VentaRepo", "Intento $attempt/$maxRetries falló: ${e.message}, reintentando...")
                
                // Si no hay red, no seguimos intentando en este ciclo, el Worker o el MainContainer lo harán luego
                val msg = e.message?.lowercase() ?: ""
                val isNetworkError = e is ServerUnreachableException || msg.contains("host") || msg.contains("connect")
                if (isNetworkError) break
            }

            if (attempt < maxRetries) {
                delay(2000)
            }
        }

        // Si llegamos aquí, fallaron todos los intentos o fue un error fatal
        ventaColaDao.updateEstado(venta.idTemporal, "ERROR")
        throw lastError ?: Exception("Error desconocido al sincronizar venta ${venta.idTemporal}")
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
                    Log.w("VentaRepo", "Fallo al sincronizar venta ${venta.idTemporal}, continuando con la siguiente.")
                }
            } catch (e: Exception) {
                Log.e("VentaRepo", "Error inesperado en sincronización para venta ${venta.idTemporal}", e)
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

    suspend fun mergeIntoCola(request: VentaRequest, idTemporal: String): Boolean {
        val existente = ventaColaDao.getById(idTemporal) ?: return false
        val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
        val existentes: List<ItemCarritoDto> = try {
            gson.fromJson(existente.productosJson, type)
        } catch (e: Exception) {
            emptyList()
        }
        val merged = (existentes + request.productos)
            .groupBy { it.claveProducto to it.observaciones to it.modificadores }
            .map { (_, list) ->
                list.first().copy(cantidad = list.sumOf { it.cantidad })
            }
        val updated = existente.copy(
            productosJson = gson.toJson(merged),
            totalVenta = merged.sumOf { (it.cantidad * (it.precio ?: 0.0)) + it.modificadores.sumOf { m -> m.cantidad * (m.precio ?: 0.0) } },
            fechaCreacion = System.currentTimeMillis(),
            estado = "PENDIENTE"
        )
        ventaColaDao.insert(updated)
        Log.d("VentaRepo", "Productos mergeados localmente en cola $idTemporal")
        return true
    }
}
private fun VentaColaEntity.toVentaDto(): VentaDto {
    val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    val displayDate = sdfDate.format(java.util.Date(fechaCreacion))
    val displayTime = sdfTime.format(java.util.Date(fechaCreacion))

    val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
    val productosVenta = try {
        val items: List<ItemCarritoDto> = Gson().fromJson(productosJson, type) ?: emptyList()
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
                idEstado = if (item.printDefault) "0" else "",
                modificadores = item.modificadores
            )
        }
    } catch (e: Exception) {
        emptyList()
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
            val items: List<ItemCarritoDto> = gson.fromJson(productosJson, type) ?: emptyList()
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
            gson.fromJson(productosJson, object : TypeToken<List<ProductoVentaDto>>() {}.type) ?: emptyList()
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
        gson.fromJson(productosJson, object : TypeToken<List<ProductoVentaDto>>() {}.type) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
    val pagos: List<PagoDto> = try {
        gson.fromJson(pagosJson, object : TypeToken<List<PagoDto>>() {}.type) ?: emptyList()
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

fun VentaDto.toVentaRecibidaEntity(): VentaRecibidaEntity {
    val gson = Gson()
    val fullFecha = if (!fecha.isNullOrBlank() && !hora.isNullOrBlank()) "$fecha $hora:00" else fecha ?: ""
    return VentaRecibidaEntity(
        folio = folio,
        fecha = fullFecha,
        total = total,
        estado = estatus,
        clienteNombre = nombreCliente,
        socioId = socioId,
        clavePuntoVenta = clavePuntoVenta,
        corteCaja = cajaId ?: 0,
        productosJson = gson.toJson(productos),
        pagosJson = gson.toJson(pagos)
    )
}
