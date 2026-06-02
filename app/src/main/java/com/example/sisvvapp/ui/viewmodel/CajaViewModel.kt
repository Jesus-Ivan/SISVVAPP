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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CajaViewModel(
    private val cajaRepository: CajaRepository,
    private val socioRepository: SocioRepository,
    private val productoRepository: ProductoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _cajas = MutableStateFlow<List<CajaActivaEntity>>(emptyList())
    val cajas: StateFlow<List<CajaActivaEntity>> = _cajas

    private val _selectedCajaId = MutableStateFlow<Int?>(null)
    val selectedCajaId: StateFlow<Int?> = _selectedCajaId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val savedCajaId = sessionManager.getSelectedCajaId()

    init {
        _selectedCajaId.value = savedCajaId
        observeCajas()
        sync()
    }

    private fun observeCajas() {
        viewModelScope.launch {
            cajaRepository.getCajasAbiertas().collect { lista ->
                _cajas.value = lista
                if (_selectedCajaId.value == null && lista.isNotEmpty()) {
                    _selectedCajaId.value = lista.first().id
                }
            }
        }
    }

    fun selectCaja(id: Int, nombre: String = "") {
        _selectedCajaId.value = id
        sessionManager.saveSelectedCaja(id, nombre)
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = cajaRepository.sync()
            result.onFailure { e ->
                Log.e("CajaVM", "Sync falló", e)
                _errorMessage.value = e.message ?: "Error al sincronizar cajas"
            }
            result.onSuccess {
                sessionManager.saveLastSyncDate(System.currentTimeMillis())
            }
            _isLoading.value = false
        }
        // Sync de catálogos en background (no bloquea la UI)
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
