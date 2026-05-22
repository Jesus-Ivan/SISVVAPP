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

    private val _cajaActiva = MutableStateFlow<CajaActivaEntity?>(null)
    val cajaActiva: StateFlow<CajaActivaEntity?> = _cajaActiva

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeCaja()
        sync()
    }

    private fun observeCaja() {
        viewModelScope.launch {
            cajaRepository.getCajaActiva().collect { caja ->
                _cajaActiva.value = caja
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cajaRepository.sync()
            } catch (e: Exception) {
                Log.e("CajaVM", "Sync falló", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
