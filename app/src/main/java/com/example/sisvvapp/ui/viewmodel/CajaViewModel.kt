package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.data.repository.CajaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CajaViewModel(
    private val cajaRepository: CajaRepository
) : ViewModel() {
    private val _cajas = MutableStateFlow<List<CajaActivaEntity>>(emptyList())
    val cajas: StateFlow<List<CajaActivaEntity>> = _cajas
    private val _selectedCajaId = MutableStateFlow<Int?>(null)
    val selectedCajaId: StateFlow<Int?> = _selectedCajaId
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    init {
        observeCajas()
        sync()
    }
    private fun observeCajas() {
        viewModelScope.launch {
            cajaRepository.getCajasAbiertas().collect { lista ->
                _cajas.value = lista
                if (lista.isNotEmpty() && _selectedCajaId.value == null) {
                    _selectedCajaId.value = lista.first().id
                }
            }
        }
    }
    fun selectCaja(id: Int) {
        _selectedCajaId.value = id
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
            _isLoading.value = false
        }
    }
}
