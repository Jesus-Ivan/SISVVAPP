package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.data.repository.CajaRepository
import com.example.sisvvapp.data.repository.ProductoRepository
import com.example.sisvvapp.data.repository.SocioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CajaViewModel(
    private val cajaRepository: CajaRepository,
    private val socioRepository: SocioRepository,
    private val productoRepository: ProductoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val cajas: StateFlow<List<CajaActivaEntity>> = cajaRepository.getCajasAbiertas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCajaId = MutableStateFlow<Int?>(null)
    val selectedCajaId: StateFlow<Int?> = _selectedCajaId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        _selectedCajaId.value = sessionManager.getSelectedCajaId()
        cajas.onEach { lista ->
            if (lista.isNotEmpty()) {
                val seleccionValida = _selectedCajaId.value?.let { id ->
                    lista.any { it.id == id }
                } ?: false
                if (!seleccionValida) {
                    _selectedCajaId.value = lista.first().id
                    sessionManager.saveSelectedCaja(lista.first().id, lista.first().nombre)
                }
            }
        }.launchIn(viewModelScope)
        sync()
    }

    fun selectCaja(id: Int, nombre: String = "") {
        _selectedCajaId.value = id
        sessionManager.saveSelectedCaja(id, nombre)
    }

    suspend fun refreshCajas(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        val result = cajaRepository.sync()
        result.onSuccess {
            sessionManager.saveLastSyncDate(System.currentTimeMillis())
        }
        _isLoading.value = false
        return result.isSuccess
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = cajaRepository.sync()
                result.fold(
                    onSuccess = { sessionManager.saveLastSyncDate(System.currentTimeMillis()) },
                    onFailure = { e ->
                        Log.e("CajaVM", "Sync falló", e)
                        _errorMessage.value = e.message ?: "Error al sincronizar cajas"
                    }
                )
            } catch (e: Exception) {
                Log.e("CajaVM", "Sync falló", e)
                _errorMessage.value = e.message ?: "Error al sincronizar cajas"
            } finally {
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            socioRepository.sync().onFailure { e ->
                Log.w("CajaVM", "Sync socios falló", e)
            }
        }
        viewModelScope.launch {
            productoRepository.sync().onFailure { e ->
                Log.w("CajaVM", "Sync productos falló", e)
            }
        }
    }
}
