package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.ApiResult
import com.example.sisvvapp.network.dto.ventas.VentaDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class VentasUiState {
    object Loading : VentasUiState()
    data class Success(val ventas: List<VentaDto>, val isRefreshing: Boolean = false) : VentasUiState()
    object Empty : VentasUiState()
    data class Error(val message: String) : VentasUiState()
    data class NetworkError(val message: String, val ventasLocales: List<VentaDto>) : VentasUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class VentasViewModel(
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isNetworkError = MutableStateFlow(false)

    // Ambos valores (corte + fecha) controlan qué ventas se muestran en Room
    private val _corteCaja = MutableStateFlow<Int?>(null)
    private val _fecha = MutableStateFlow(java.time.LocalDate.now().toString())

    private val _ventasData: StateFlow<List<VentaDto>> =
        combine(_corteCaja, _fecha) { corte, fecha -> Pair(corte, fecha) }
            .flatMapLatest { (corte, fecha) ->
                if (corte == null) flowOf(emptyList())
                else ventaRepository.getVentasGlobales(corte, fecha)
            }.map { ventas -> ventas.filter { it.estatus.equals("Abierta", ignoreCase = true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<VentasUiState> = combine(_ventasData, _isLoading, _error, _isNetworkError) { data, loading, error, isNetError ->
        when {
            // Caso 1: Está cargando (primer arranque o recarga manual)
            loading -> VentasUiState.Loading
            
            // Caso 2: Hubo un error de red específico (Offline)
            isNetError -> VentasUiState.NetworkError(error ?: "Error de red", data)
            
            // Caso 3: No está cargando, no hay error, pero la lista está vacía
            !loading && data.isEmpty() && error == null -> VentasUiState.Empty
            
            // Caso 4: Hubo un error general (no de red) y no hay datos
            error != null && data.isEmpty() -> VentasUiState.Error(error)
            
            // Caso 5: Tenemos datos (locales o remotos). 
            // isRefreshing será true si _isLoading es true, permitiendo que la UI muestre el banner de "Conectando...".
            else -> VentasUiState.Success(data, isRefreshing = loading)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VentasUiState.Loading)

    val pendientesCount: StateFlow<Int> = ventaRepository.getPendientesCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun refreshVentas(fecha: String, corteCaja: Int? = null) {
        _fecha.value = fecha
        _corteCaja.value = corteCaja
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isNetworkError.value = false
            
            val result = ventaRepository.syncVentas(fecha, corteCaja)
            when (result) {
                is ApiResult.Success -> {
                    // OK
                }
                is ApiResult.NetworkError -> {
                    Log.w("VentasVM", "Error de red, usando locales")
                    _error.value = result.message
                    _isNetworkError.value = true
                }
                is ApiResult.ServerError -> {
                    Log.e("VentasVM", "Error de servidor: ${result.code}")
                    _error.value = "Error de servidor: ${result.message}"
                }
                is ApiResult.EmptyData -> {
                    // No hay ventas, sync exitoso pero sin datos
                }
            }
            _isLoading.value = false
        }
    }

    suspend fun cargarDetalle(id: String): VentaDto? {
        return ventaRepository.getVentaDetalleGlobal(id)
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

    fun reimprimirComanda(folio: Int, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = ventaRepository.reimprimirComanda(folio)
            if (result.isSuccess) {
                Log.d("VentasVM", "Reimpresión exitosa para folio $folio")
            }
            onResult(result)
        }
    }
}
