package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.dto.ventas.VentaDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VentasViewModel(
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _corteCaja = MutableStateFlow<Int?>(null)

    val ventas: StateFlow<List<VentaDto>> = _corteCaja.flatMapLatest { corte ->
        if (corte != null) ventaRepository.getVentasRecibidas(corte)
        else ventaRepository.getAllVentasRecibidas()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendientesCount: StateFlow<Int> = ventaRepository.getPendientesCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var fechaActual: String = ""
        private set
    var cajaId: Int? = null
        private set

    fun refreshVentas(fecha: String, corteCaja: Int? = null) {
        fechaActual = fecha
        cajaId = corteCaja
        _corteCaja.value = corteCaja
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                ventaRepository.syncVentas(fecha, corteCaja)
            } catch (e: Exception) {
                Log.w("VentasVM", "No se pudo sync, usando datos locales", e)
                _error.value = "Modo offline - Mostrando datos locales"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun cargarDetalle(folio: Int): VentaDto? {
        return ventaRepository.getVentaDetalle(folio)
    }
}
