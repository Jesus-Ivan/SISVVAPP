package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.dto.ventas.VentaDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    // Ambos valores (corte + fecha) controlan qué ventas se muestran en Room
    private val _corteCaja = MutableStateFlow<Int?>(null)
    private val _fecha = MutableStateFlow(java.time.LocalDate.now().toString())

    val ventas: StateFlow<List<VentaDto>> =
        combine(_corteCaja, _fecha) { corte, fecha -> Pair(corte, fecha) }
            .flatMapLatest { (corte, fecha) ->
                if (corte != null) {
                    ventaRepository.getVentasPorCorteYFecha(corte, fecha)
                } else {
                    ventaRepository.getVentasPorFecha(fecha)
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendientesCount: StateFlow<Int> = ventaRepository.getPendientesCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun refreshVentas(fecha: String, corteCaja: Int? = null) {
        _fecha.value = fecha
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

    fun getVentasAbiertasDelCorte(corteCaja: Int): Flow<List<VentaDto>> {
        return ventaRepository.getVentasRecibidas(corteCaja).map { ventas ->
            ventas.filter { it.estatus.equals("Abierta", ignoreCase = true) }
        }
    }

    fun transferirProducto(
        folioOrigen: Int,
        chunk: Long,
        folioDestino: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = ventaRepository.transferirProducto(folioOrigen, chunk, folioDestino)
            if (result.isSuccess) {
                Log.d("VentasVM", "Transferencia exitosa: chunk $chunk de $folioOrigen a $folioDestino")
            }
            onResult(result)
        }
    }
}
