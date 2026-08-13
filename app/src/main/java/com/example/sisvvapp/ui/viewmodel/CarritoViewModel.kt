package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.repository.ProductoRepository
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto
import com.example.sisvvapp.network.dto.productos.ModificadorSeleccionadoDto
import com.example.sisvvapp.network.dto.ventas.VentaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CarritoItem(
    val producto: ProductoEntity,
    val cantidad: Int,
    val observaciones: String = "",
    val modificadores: List<ModificadorEntity> = emptyList(),
    val modificadorObservaciones: Map<Int, String> = emptyMap(),
    val precioUnitario: Double = producto.precio,
    val subtotal: Double = precioUnitario * cantidad,
    val printDefault: Boolean = producto.printDefault,
    val tiempo: Int = 1,
    val id: String = java.util.UUID.randomUUID().toString()
)

class CarritoViewModel(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val _tipoVenta = MutableStateFlow("")
    val tipoVenta: StateFlow<String> = _tipoVenta

    private val _socioId = MutableStateFlow<Int?>(null)
    val socioId: StateFlow<Int?> = _socioId

    private val _nombreCliente = MutableStateFlow("")
    val nombreCliente: StateFlow<String> = _nombreCliente

    private val _corteCaja = MutableStateFlow(0)
    val corteCaja: StateFlow<Int> = _corteCaja

    private val _clavePuntoVenta = MutableStateFlow("")
    val clavePuntoVenta: StateFlow<String> = _clavePuntoVenta

    private val _numComensales = MutableStateFlow<String?>(null)
    val numComensales: StateFlow<String?> = _numComensales

    private val _items = MutableStateFlow<List<CarritoItem>>(emptyList())
    val items: StateFlow<List<CarritoItem>> = _items

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _productoSeleccionado = MutableStateFlow<ProductoEntity?>(null)
    val productoSeleccionado: StateFlow<ProductoEntity?> = _productoSeleccionado

    val productos: StateFlow<List<ProductoEntity>> = combine(
        productoRepository.getProductos(),
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) {
            all.take(25)
        } else {
            all.filter { it.descripcion.contains(query, ignoreCase = true) }.take(25)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _sendResult = MutableStateFlow<SendResult?>(null)
    val sendResult: StateFlow<SendResult?> = _sendResult

    private val _appendMode = MutableStateFlow(false)

    private val _folioExistente = MutableStateFlow<Int?>(null)

    private val _idTemporalExistente = MutableStateFlow<String?>(null)

    fun configurarAppendMode(folio: Int, nombreCliente: String, clavePuntoVenta: String, tipoVenta: String, corteCaja: Int, idTemporal: String? = null) {
        _appendMode.value = true
        _folioExistente.value = if (folio > 0) folio else null
        _idTemporalExistente.value = idTemporal
        _nombreCliente.value = nombreCliente
        _clavePuntoVenta.value = clavePuntoVenta
        _tipoVenta.value = tipoVenta
        _corteCaja.value = corteCaja
    }

    fun esModoAppend(): Boolean = _appendMode.value

    fun configurarVenta(
        tipoVenta: String,
        socioId: Int?,
        nombreCliente: String,
        corteCaja: Int,
        clavePuntoVenta: String,
        numComensales: String? = null
    ) {
        val socioAnterior = _socioId.value
        val socioCambio = socioAnterior != socioId

        _appendMode.value = false
        _folioExistente.value = null
        _idTemporalExistente.value = null
        _tipoVenta.value = tipoVenta
        _socioId.value = socioId
        _nombreCliente.value = nombreCliente
        _corteCaja.value = corteCaja
        _clavePuntoVenta.value = clavePuntoVenta
        _numComensales.value = numComensales

        if (socioCambio) {
            _items.value = emptyList()
            _total.value = 0.0
        }
    }

    fun searchProductos(query: String) {
        _searchQuery.value = query
    }

    var cantidadSeleccionada: Int = 1
        private set

    fun seleccionarProducto(producto: ProductoEntity, cantidad: Int = 1) {
        cantidadSeleccionada = cantidad
        _productoSeleccionado.value = producto
    }

    fun addProducto(producto: ProductoEntity, cantidad: Int = 1, observaciones: String = "") {
        val existingIndex = _items.value.indexOfFirst {
            it.producto.id == producto.id && it.modificadores.isEmpty()&& it.observaciones == observaciones
        }
        if (existingIndex >= 0) {
            val existing = _items.value[existingIndex]
            val updated = existing.copy(
                cantidad = existing.cantidad + cantidad,
                subtotal = existing.precioUnitario * (existing.cantidad + cantidad)
            )
            _items.value = _items.value.toMutableList().apply { set(existingIndex, updated) }
        } else {
            val item = CarritoItem(
                producto = producto,
                cantidad = cantidad,
                observaciones = observaciones,
                precioUnitario = producto.precio,
                subtotal = producto.precio * cantidad
            )
            _items.value = _items.value + item
        }
        calcularTotal()
    }

    fun addProductoConModificadores(
        producto: ProductoEntity,
        modificadores: List<ModificadorEntity>,
        grupos: List<com.example.sisvvapp.data.local.entity.GrupoModificadorEntity>,
        cantidad: Int = 1,
        observaciones: String = "",
        modificadorNotas: Map<Int, String> = emptyMap()
    ) {
        val finalModificadores = mutableListOf<ModificadorEntity>()
        val modificadoresPorGrupo = modificadores.groupBy { it.grupo }

        modificadoresPorGrupo.forEach { (grupoId, mods) ->
            val grupo = grupos.find { it.idGrupo.toString() == grupoId }
            val limiteIncluidos = (grupo?.modifIncluidos ?: 0) * cantidad

            val modsOrdenados = mods.sortedBy { it.precio }
            modsOrdenados.forEachIndexed { index, mod ->
                val isIncluido = index < limiteIncluidos
                finalModificadores.add(mod.copy(incluido = isIncluido))
            }
        }

        val precioModificadores = finalModificadores.sumOf { if (it.incluido) 0.0 else it.precio }
        val precioUnitario = producto.precio + precioModificadores
        val item = CarritoItem(
            producto = producto,
            cantidad = cantidad,
            observaciones = observaciones,
            modificadores = finalModificadores,
            modificadorObservaciones = modificadorNotas,
            precioUnitario = precioUnitario,
            subtotal = precioUnitario * cantidad
        )
        _items.value = _items.value + item
        calcularTotal()
    }

    fun removeProducto(item: CarritoItem) {
        val list = _items.value.toMutableList()
        list.remove(item)
        _items.value = list
        calcularTotal()
    }

    fun insertarProducto(index: Int, item: CarritoItem) {
        // Convertimos a mutable para poder insertar
        val listaActual = _items.value.toMutableList()

        // Nos aseguramos de que el índice sea válido (por seguridad)
        val indexSeguro = index.coerceIn(0, listaActual.size)

        // Generamos una copia con un ID único para evitar reutilización de estados de deslizamiento
        val restoredItem = item.copy(id = java.util.UUID.randomUUID().toString())
        listaActual.add(indexSeguro, restoredItem)
        _items.value = listaActual
        calcularTotal()
    }

    fun updateCantidad(index: Int, cantidad: Int) {
        if (cantidad <= 0) {
            _items.value.getOrNull(index)?.let { removeProducto(it) }
            return
        }
        val item = _items.value[index]
        val updated = item.copy(
            cantidad = cantidad,
            subtotal = item.precioUnitario * cantidad
        )
        _items.value = _items.value.toMutableList().apply { set(index, updated) }
        calcularTotal()
    }

    fun updateTiempo(index: Int, tiempo: Int?) {
        val tiempoNormalizado = tiempo ?: 1
        if (tiempoNormalizado !in 1..4) return
        val item = _items.value.getOrNull(index) ?: return
        val updated = item.copy(tiempo = tiempoNormalizado)
        _items.value = _items.value.toMutableList().apply { set(index, updated) }
    }

    private fun calcularTotal() {
        _total.value = _items.value.sumOf { it.subtotal }
    }


    fun confirmarVenta() {
        if (_items.value.isEmpty()) return

        viewModelScope.launch {
            _isSending.value = true
            _sendResult.value = null

            val productosRaw = _items.value.map { item ->
                // Group duplicate modifiers by claveModificador + incluido para separar
                // los incluidos (precio 0) de los de pago en líneas distintas del ticket
                val modificadores = item.modificadores
                    .groupBy { it.claveModificador to it.incluido }
                    .map { (_, mods) ->
                        ModificadorSeleccionadoDto(
                            claveProducto = mods.first().claveModificador,
                            cantidad = mods.size,
                            precio = if (mods.first().incluido) 0.0 else mods.first().precio,
                            nombre = mods.first().nombre,
                            observaciones = item.modificadorObservaciones[mods.first().id] ?: ""
                        )
                    }
                ItemCarritoDto(
                    claveProducto = item.producto.id,
                    cantidad = item.cantidad,
                    observaciones = item.observaciones,
                    modificadores = modificadores,
                    nombre = item.producto.descripcion,
                    precio = item.producto.precio,
                    printDefault = item.producto.printDefault,
                    tiempo = item.tiempo
                )
            }

            // AGREGAR: Agrupamos productos por claveProducto, observaciones, modificadores y tiempo para evitar filas duplicadas
            val productos = productosRaw.groupBy { it.claveProducto to it.observaciones to it.modificadores to it.tiempo }.map { (_, list) ->
                list.first().copy(
                    cantidad = list.sumOf { it.cantidad }
                )
            }

            // Generamos o usamos el ID temporal para el request_id
            val idTemporal = _idTemporalExistente.value ?: java.util.UUID.randomUUID().toString()

            // El tiempo se conserva aquí (persistencia local/cola). El dato al API
            // se omite en VentaRepository (tiempo = null) para no enviarlo al servidor.
            val request = VentaRequest(
                requestId = idTemporal,
                corteCaja = _corteCaja.value,
                tipoVenta = _tipoVenta.value,
                idSocio = _socioId.value,
                nombre = _nombreCliente.value,
                clavePuntoVenta = _clavePuntoVenta.value,
                numComensales = _numComensales.value,
                productos = productos,
                total = _total.value
            )

            if (_appendMode.value && _folioExistente.value != null && _folioExistente.value!! > 0) {
                val folio = _folioExistente.value!!
                val result = ventaRepository.appendProductos(folio, request, idTemporal)
                result.fold(
                    onSuccess = {
                        Log.d("CarritoVM", "Productos agregados a venta $folio")
                        _sendResult.value = SendResult.Success(folio)
                    },
                    onFailure = { e ->
                        if (e.message?.contains("offline") == true) {
                            _sendResult.value = SendResult.Offline
                        } else if (e.message?.contains("caja_cerrada") == true) {
                            Log.w("CarritoVM", "Caja cerrada al agregar productos, se muestra diálogo de caja cerrada")
                            _sendResult.value = null
                        } else {
                            Log.e("CarritoVM", "Error al agregar productos", e)
                            _sendResult.value = SendResult.Error(e.message ?: "Error desconocido")
                        }
                    }
                )
            } else if (_appendMode.value && _idTemporalExistente.value != null) {
                // Venta offline sin folio real → merge local directo a la cola
                val merged = ventaRepository.mergeIntoCola(request, _idTemporalExistente.value!!)
                if (merged) {
                    _sendResult.value = SendResult.Success(0)
                } else {
                    Log.w("CarritoVM", "Entity ${_idTemporalExistente.value} no encontrado en cola, creando nuevo registro offline")
                    ventaRepository.encolarVentaOffline(request, _folioExistente.value, null)
                    _sendResult.value = SendResult.Success(0)
                }
            } else {
                val result = ventaRepository.crearVenta(request, idTemporal)
                result.fold(
                    onSuccess = { response ->
                        Log.d("CarritoVM", "Venta creada: folio ${response.folio}")
                        _sendResult.value = SendResult.Success(response.folio)
                    },
                    onFailure = { e ->
                        if (e.message?.contains("offline") == true) {
                            _sendResult.value = SendResult.Offline
                        } else if (e.message?.contains("caja_cerrada") == true) {
                            Log.w("CarritoVM", "Caja cerrada al crear venta, se muestra diálogo de caja cerrada")
                            _sendResult.value = null
                        } else {
                            Log.e("CarritoVM", "Error al crear venta", e)
                            _sendResult.value = SendResult.Error(e.message ?: "Error desconocido")
                        }
                    }
                )
            }
            _isSending.value = false
        }
    }

    fun clearState() {
        _tipoVenta.value = ""
        _socioId.value = null
        _nombreCliente.value = ""
        _corteCaja.value = 0
        _clavePuntoVenta.value = ""
        _items.value = emptyList()
        _total.value = 0.0
        _sendResult.value = null
        _searchQuery.value = ""
        _productoSeleccionado.value = null
        _isSending.value = false
        _appendMode.value = false
        _folioExistente.value = null
        _idTemporalExistente.value = null
    }
}

sealed class SendResult {
    data class Success(val folio: Int) : SendResult()
    data object Offline : SendResult()
    data class Error(val message: String) : SendResult()
}
