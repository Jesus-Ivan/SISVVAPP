package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.dao.ProductoConModificadores
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.data.repository.ProductoRepository
import com.example.sisvvapp.data.repository.SocioRepository
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto
import com.example.sisvvapp.network.dto.productos.ModificadorSeleccionadoDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class NuevaVentaStep {
    PRODUCT_SEARCH,
    MODIFIER_SELECTION,
    PARTNER_SEARCH,
    CONFIRMATION
}

class NuevaVentaViewModel(
    private val productoRepository: ProductoRepository,
    private val socioRepository: SocioRepository,
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val gson = Gson()

    // ── Step ────────────────────────────────────────────────────────────────
    private val _currentStep = MutableStateFlow(NuevaVentaStep.PRODUCT_SEARCH)
    val currentStep: StateFlow<NuevaVentaStep> = _currentStep

    // ── Product search ──────────────────────────────────────────────────────
    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos

    private val _productoConMods = MutableStateFlow<ProductoConModificadores?>(null)
    val productoConMods: StateFlow<ProductoConModificadores?> = _productoConMods

    private val _selectedModIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedModIds: StateFlow<Set<Int>> = _selectedModIds

    // ── Cart ────────────────────────────────────────────────────────────────
    private val _carrito = MutableStateFlow<List<ItemCarritoDto>>(emptyList())
    val carrito: StateFlow<List<ItemCarritoDto>> = _carrito

    // ── Partner search ──────────────────────────────────────────────────────
    private val _socios = MutableStateFlow<List<SocioEntity>>(emptyList())
    val socios: StateFlow<List<SocioEntity>> = _socios

    private val _socioSeleccionado = MutableStateFlow<SocioEntity?>(null)
    val socioSeleccionado: StateFlow<SocioEntity?> = _socioSeleccionado

    // ── Caja info (set externally) ─────────────────────────────────────────┐
    var corteCaja: Int = 0
    var clavePuntoVenta: String = ""
    var nombreCaja: String = ""

    // ── Observables ─────────────────────────────────────────────────────────
    val totalCarrito: Double get() = _carrito.value.sumOf { it.cantidad * (calcularPrecioProducto(it.claveProducto) ?: 0.0) }

    private fun calcularPrecioProducto(clave: Int): Double? {
        val prod = _productos.value.find { it.id == clave }
        return prod?.precio
    }

    // ── Step 1: Product Search ──────────────────────────────────────────────
    fun searchProductos(query: String) {
        viewModelScope.launch {
            productoRepository.searchProductos("%$query%").collect { lista ->
                _productos.value = lista
            }
        }
    }

    fun seleccionarProducto(clave: Int) {
        viewModelScope.launch {
            val conMods = productoRepository.getProductoConModificadores(clave)
            if (conMods != null && conMods.modificadores.isNotEmpty()) {
                _productoConMods.value = conMods
                _selectedModIds.value = emptySet()
                _currentStep.value = NuevaVentaStep.MODIFIER_SELECTION
            } else {
                agregarAlCarrito(clave, emptyList())
            }
        }
    }

    // ── Step 2: Modifier Selection ──────────────────────────────────────────
    fun toggleModificador(modId: Int) {
        val current = _selectedModIds.value.toMutableSet()
        if (current.contains(modId)) current.remove(modId) else current.add(modId)
        _selectedModIds.value = current
    }

    fun confirmarModificadores() {
        val conMods = _productoConMods.value ?: return
        val selectedMods = conMods.modificadores.filter { it.id in _selectedModIds.value }
        val modDtos = selectedMods.map { m ->
            ModificadorSeleccionadoDto(
                claveProducto = m.id,
                cantidad = 1,
                precio = m.precio
            )
        }
        agregarAlCarrito(conMods.producto.id, modDtos)
    }

    private fun agregarAlCarrito(claveProducto: Int, modificadores: List<ModificadorSeleccionadoDto>) {
        val item = ItemCarritoDto(
            claveProducto = claveProducto,
            cantidad = 1,
            modificadores = modificadores
        )
        _carrito.value = _carrito.value + item
        _productoConMods.value = null
        _selectedModIds.value = emptySet()
        _currentStep.value = NuevaVentaStep.PRODUCT_SEARCH
    }

    fun eliminarDelCarrito(index: Int) {
        val list = _carrito.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _carrito.value = list
        }
    }

    // ── Step 4: Partner Search ──────────────────────────────────────────────
    fun irASeleccionarSocio() {
        _currentStep.value = NuevaVentaStep.PARTNER_SEARCH
    }

    fun searchSocios(query: String) {
        viewModelScope.launch {
            socioRepository.searchSocios("%$query%").collect { lista ->
                _socios.value = lista
            }
        }
    }

    fun seleccionarSocio(socio: SocioEntity) {
        _socioSeleccionado.value = socio
        _currentStep.value = NuevaVentaStep.CONFIRMATION
    }

    fun saltarSocio(nombre: String) {
        _socioSeleccionado.value = null
        _currentStep.value = NuevaVentaStep.CONFIRMATION
    }

    // ── Step 5: Save ────────────────────────────────────────────────────────
    fun guardarVentaOffline(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val socio = _socioSeleccionado.value
                val productosJson = gson.toJson(_carrito.value)
                val total = _carrito.value.sumOf { item ->
                    val precioBase = calcularPrecioProducto(item.claveProducto) ?: 0.0
                    val precioMods = item.modificadores.sumOf { it.precio ?: 0.0 }
                    (precioBase + precioMods) * item.cantidad
                }

                val nombreCliente = socio?.let {
                    if (it.apellidoM.isNullOrBlank()) "${it.nombre} ${it.apellidoP}"
                    else "${it.nombre} ${it.apellidoP} ${it.apellidoM}"
                } ?: "Invitado"

                val venta = VentaColaEntity(
                    idTemporal = UUID.randomUUID().toString(),
                    tipoVenta = if (socio != null) "socio" else "invitado",
                    idSocio = socio?.id,
                    nombreCliente = nombreCliente,
                    corteCaja = corteCaja,
                    clavePuntoVenta = clavePuntoVenta,
                    nombreCaja = nombreCaja,
                    productosJson = productosJson,
                    fechaCreacion = System.currentTimeMillis(),
                    totalVenta = total,
                    estado = "PENDIENTE"
                )

                ventaRepository.encolarVenta(venta)
                Log.d("NvaVentaVM", "Venta ${venta.idTemporal} guardada offline")
                onSuccess()
            } catch (e: Exception) {
                Log.e("NvaVentaVM", "Error al guardar venta", e)
            }
        }
    }

    // ── Navigation helpers ──────────────────────────────────────────────────
    fun volverAListaProductos() {
        _currentStep.value = NuevaVentaStep.PRODUCT_SEARCH
    }

    fun cancelarVenta() {
        _carrito.value = emptyList()
        _socioSeleccionado.value = null
        _currentStep.value = NuevaVentaStep.PRODUCT_SEARCH
    }
}
