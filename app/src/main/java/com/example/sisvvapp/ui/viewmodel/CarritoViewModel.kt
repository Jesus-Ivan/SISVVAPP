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
    val precioUnitario: Double = producto.precio,
    val subtotal: Double = precioUnitario * cantidad,
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

    private val _items = MutableStateFlow<List<CarritoItem>>(emptyList())
    val items: StateFlow<List<CarritoItem>> = _items

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val productos: StateFlow<List<ProductoEntity>> = combine(
        productoRepository.getProductos(),
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) {
            all.take(15)
        } else {
            all.filter { it.descripcion.contains(query, ignoreCase = true) }.take(15)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _sendResult = MutableStateFlow<SendResult?>(null)
    val sendResult: StateFlow<SendResult?> = _sendResult

    private val _productoSeleccionado = MutableStateFlow<ProductoEntity?>(null)
    val productoSeleccionado: StateFlow<ProductoEntity?> = _productoSeleccionado

    private val _appendMode = MutableStateFlow(false)
    val appendMode: StateFlow<Boolean> = _appendMode

    private val _folioExistente = MutableStateFlow<Int?>(null)
    val folioExistente: StateFlow<Int?> = _folioExistente

    fun configurarAppendMode(folio: Int, nombreCliente: String, clavePuntoVenta: String, tipoVenta: String) {
        _appendMode.value = true
        _folioExistente.value = folio
        _nombreCliente.value = nombreCliente
        _clavePuntoVenta.value = clavePuntoVenta
        _tipoVenta.value = tipoVenta
    }

    fun esModoAppend(): Boolean = _appendMode.value

    fun configurarVenta(
        tipoVenta: String,
        socioId: Int?,
        nombreCliente: String,
        corteCaja: Int,
        clavePuntoVenta: String
    ) {
        _tipoVenta.value = tipoVenta
        _socioId.value = socioId
        _nombreCliente.value = nombreCliente
        _corteCaja.value = corteCaja
        _clavePuntoVenta.value = clavePuntoVenta
    }

    fun searchProductos(query: String) {
        _searchQuery.value = query
    }

    var cantidadSeleccionada: Int = 1
        private set

    fun seleccionarProducto(producto: ProductoEntity, cantidad: Int = 1) {
        _productoSeleccionado.value = producto
        cantidadSeleccionada = cantidad
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
        observaciones: String = ""
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

    private fun calcularTotal() {
        _total.value = _items.value.sumOf { it.subtotal }
    }


    fun confirmarVenta() {
        if (_items.value.isEmpty()) return

        viewModelScope.launch {
            _isSending.value = true
            _sendResult.value = null

            val productos = _items.value.map { item ->
                // Group duplicate modifiers by claveModificador to aggregate quantity and price
                val modificadores = item.modificadores.groupBy { it.claveModificador }.map { (claveModificador, mods) ->
                    val totalPrecio = mods.sumOf { if (it.incluido) 0.0 else it.precio }
                    // We send the average price per modifier unit, or send the sum divided by quantity
                    ModificadorSeleccionadoDto(
                        claveProducto = claveModificador,
                        cantidad = mods.size,
                        precio = totalPrecio / mods.size
                    )
                }
                ItemCarritoDto(
                    claveProducto = item.producto.id,
                    cantidad = item.cantidad,
                    observaciones = item.observaciones,
                    modificadores = modificadores
                )
            }

            val request = VentaRequest(
                corteCaja = _corteCaja.value,
                tipoVenta = _tipoVenta.value,
                idSocio = _socioId.value,
                nombre = _nombreCliente.value,
                clavePuntoVenta = _clavePuntoVenta.value,
                productos = productos
            )

            if (_appendMode.value && _folioExistente.value != null) {
                val folio = _folioExistente.value!!
                val result = ventaRepository.appendProductos(folio, request)
                result.fold(
                    onSuccess = {
                        Log.d("CarritoVM", "Productos agregados a venta $folio")
                        _sendResult.value = SendResult.Success(folio)
                    },
                    onFailure = { e ->
                        if (e.message?.contains("offline") == true) {
                            _sendResult.value = SendResult.Offline
                        } else {
                            Log.e("CarritoVM", "Error al agregar productos", e)
                            _sendResult.value = SendResult.Error(e.message ?: "Error desconocido")
                        }
                    }
                )
            } else {
                val result = ventaRepository.crearVenta(request)
                result.fold(
                    onSuccess = { response ->
                        Log.d("CarritoVM", "Venta creada: folio ${response.folio}")
                        _sendResult.value = SendResult.Success(response.folio)
                    },
                    onFailure = { e ->
                        if (e.message?.contains("offline") == true) {
                            _sendResult.value = SendResult.Offline
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

    fun limpiarCarrito() {
        _items.value = emptyList()
        _total.value = 0.0
        _sendResult.value = null
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
    }
}

sealed class SendResult {
    data class Success(val folio: Int) : SendResult()
    data object Offline : SendResult()
    data class Error(val message: String) : SendResult()
}
