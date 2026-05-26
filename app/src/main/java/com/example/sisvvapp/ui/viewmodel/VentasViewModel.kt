package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.dto.ventas.VentaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VentasViewModel(
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val _ventas = MutableStateFlow<List<VentaDto>>(emptyList())
    val ventas: StateFlow<List<VentaDto>> = _ventas

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _pendientesCount = MutableStateFlow(0)
    val pendientesCount: StateFlow<Int> = _pendientesCount

    var fechaActual: String = ""
        private set

    var cajaId: Int? = null
        private set

    fun loadVentas(fecha: String, corteCaja: Int? = null) {
        fechaActual = fecha
        cajaId = corteCaja
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lista = ventaRepository.getVentas(fecha, corteCaja)
                _ventas.value = lista
                Log.d("VentasVM", "Cargadas ${lista.size} ventas para $fecha")
            } catch (e: Exception) {
                Log.e("VentasVM", "Error al cargar ventas", e)
                _error.value = "Error al cargar ventas"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun observePendientes() {
        viewModelScope.launch {
            ventaRepository.getPendientesCountFlow().collect { count ->
                _pendientesCount.value = count
            }
        }
    }
}
