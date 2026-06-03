package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.repository.SocioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NuevaVentaViewModel(
    private val socioRepository: SocioRepository
) : ViewModel() {

    private val _tipoVenta = MutableStateFlow("Público General")
    val tipoVenta: StateFlow<String> = _tipoVenta

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
        _tipoVenta.value = tipo
        clearSocioSelection()
    }

    fun searchSocios(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.length < 2) {
            _sociosEncontrados.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            Log.d("NuevaVentaVM", "Buscando socios con query: $query")

            socioRepository.searchSocios("%$query%").collect { lista ->

                val sociosActivos = lista.filter { socio ->
                    socio.estatus?.equals("Activo", ignoreCase = true) == true

                }

                Log.d("NuevaVentaVM", "Resultados brutos: ${lista.size} | Resultados activos (filtrados): ${sociosActivos.size}")
                _sociosEncontrados.value = sociosActivos
            }
        }
    }

    fun selectSocio(socio: SocioEntity) {
        Log.d("NuevaVentaVM", "Socio seleccionado: ID ${socio.id} - ${socio.nombre}")
        _socioSeleccionado.value = socio
        _socioId.value = socio.id

        if (_tipoVenta.value == "Socio") {
            // Es el socio directamente -> autocompletamos su nombre
            _nombreCliente.value = "${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}".trim()
        } else if (_tipoVenta.value == "Invitado del Socio") {
            // Es un invitado -> dejamos el campo limpio para que lo escriban a mano
            _nombreCliente.value = ""
        }

        _sociosEncontrados.value = emptyList()
        _searchQuery.value = ""
    }

    fun setNombreCliente(nombre: String) {
        _nombreCliente.value = nombre
    }

    fun clearSocioSelection() {
        Log.d("NuevaVentaVM", "Limpiando selección de socio")
        _socioSeleccionado.value = null
        _socioId.value = null
        _nombreCliente.value = ""
        _searchQuery.value = ""
        _sociosEncontrados.value = emptyList()
    }

    fun isFormValid(): Boolean {
        return when (_tipoVenta.value) {
            "Socio" -> _socioId.value != null
            "Invitado del Socio" -> _socioId.value != null && _nombreCliente.value.isNotBlank()
            else -> _nombreCliente.value.isNotBlank()
        }
    }
}