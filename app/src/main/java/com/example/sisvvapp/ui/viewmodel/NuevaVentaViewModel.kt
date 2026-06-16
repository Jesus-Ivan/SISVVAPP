package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.repository.SocioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class NuevaVentaViewModel(
    private val socioRepository: SocioRepository,
    private val tipoVentaRepository: com.example.sisvvapp.data.repository.TipoVentaRepository
) : ViewModel() {

    private val _allTiposVenta = MutableStateFlow<List<String>>(emptyList())
    private val _isRestricted = MutableStateFlow(false)
    val isRestricted: StateFlow<Boolean> = _isRestricted

    val tiposVenta: StateFlow<List<String>> = combine(_allTiposVenta, _isRestricted) { tipos, restricted ->
        if (restricted) {
            tipos.filter { it == "socio" || it == "invitado" }
        } else {
            tipos
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tipoVenta = MutableStateFlow("socio")
    val tipoVenta: StateFlow<String> = _tipoVenta

    init {
        viewModelScope.launch {
            tipoVentaRepository.getTiposVentaFlow().take(1).collect { entities ->
                val nombres = entities.map { it.nombre }
                _allTiposVenta.value = if (nombres.isNotEmpty()) nombres else listOf("socio", "invitado", "general", "empleado")
                if (!tiposVenta.value.contains(_tipoVenta.value)) {
                    _tipoVenta.value = tiposVenta.value.firstOrNull() ?: "socio"
                }
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sociosEncontrados = MutableStateFlow<List<SocioEntity>>(emptyList())
    val sociosEncontrados: StateFlow<List<SocioEntity>> = _sociosEncontrados

    private val _socioSeleccionado = MutableStateFlow<SocioEntity?>(null)
    val socioSeleccionado: StateFlow<SocioEntity?> = _socioSeleccionado

    private val _nombreCliente = MutableStateFlow("")
    val nombreCliente: StateFlow<String> = _nombreCliente

    private val _socioId = MutableStateFlow<Int?>(null)
    val socioId: StateFlow<Int?> = _socioId

    private var searchJob: Job? = null

    fun setTipoVenta(tipo: String) {
        val previousType = _tipoVenta.value
        _tipoVenta.value = tipo

        if (tipo != "socio" && tipo != "invitado") {
            clearSocioSelection()
        } else {
            // Mantener la selección del socio si existe
            val socio = _socioSeleccionado.value
            if (socio != null) {
                if (tipo == "socio") {
                    _nombreCliente.value = "${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}".trim()
                } else if (previousType == "socio") {
                    // Solo limpiamos si venimos de "socio", para no borrar lo que el usuario haya escrito en "invitado"
                    _nombreCliente.value = ""
                }
            }
        }
    }

    fun setRestrictedMode(restricted: Boolean) {
        _isRestricted.value = restricted
        // Si entramos en modo restringido y el tipo actual no es permitido, reseteamos a "socio"
        if (restricted && _tipoVenta.value != "socio" && _tipoVenta.value != "invitado") {
            _tipoVenta.value = "socio"
        }
    }

    fun selectSocioById(id: Int) {
        viewModelScope.launch {
            val socio = socioRepository.getSocioById(id)
            if (socio != null) {
                _tipoVenta.value = "socio"
                selectSocio(socio)
            }
        }
    }

    fun searchSocios(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.length < 1) {
            _sociosEncontrados.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            Log.d("NuevaVentaVM", "Buscando socios con query: $query")

            socioRepository.searchSocios("%$query%").collect { lista ->

                Log.d("NuevaVentaVM", "Resultados de búsqueda de socios: ${lista.size}")
                _sociosEncontrados.value = lista
            }
        }
    }

    fun selectSocio(socio: SocioEntity) {
        searchJob?.cancel()
        Log.d("NuevaVentaVM", "Socio seleccionado: ID ${socio.id} - ${socio.nombre}")
        _socioSeleccionado.value = socio
        _socioId.value = socio.id

        if (_tipoVenta.value == "socio") {
            // Es el socio directamente -> autocompletamos su nombre
            _nombreCliente.value = "${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}".trim()
        } else if (_tipoVenta.value == "invitado") {
            // Es un invitado -> dejamos el campo limpio para que lo escriban a mano
            _nombreCliente.value = ""
        }

        _sociosEncontrados.value = emptyList()
        _searchQuery.value = ""
    }

    fun setNombreCliente(nombre: String) {
        if (_tipoVenta.value != "socio") {
            _nombreCliente.value = nombre
        }
    }

    fun clearSocioSelection() {
        searchJob?.cancel()
        Log.d("NuevaVentaVM", "Limpiando selección de socio")
        _socioSeleccionado.value = null
        _socioId.value = null
        _nombreCliente.value = ""
        _searchQuery.value = ""
        _sociosEncontrados.value = emptyList()
    }

    fun resetFormulario() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _sociosEncontrados.value = emptyList()
        _socioSeleccionado.value = null
        _nombreCliente.value = ""
        _socioId.value = null
    }

    fun isFormValid(): Boolean {
        val requiereSocio = _tipoVenta.value == "socio" || _tipoVenta.value == "invitado"
        if (requiereSocio) {
            val socio = _socioSeleccionado.value
            if (socio == null || socio.estatus == "CAN") {
                return false
            }
        }
        return when (_tipoVenta.value) {
            "socio" -> _socioId.value != null
            "invitado" -> _socioId.value != null && _nombreCliente.value.isNotBlank()
            else -> _nombreCliente.value.isNotBlank()
        }
    }
}