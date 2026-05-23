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

    // Lista de todas las cajas abiertas
    private val _cajas = MutableStateFlow<List<CajaActivaEntity>>(emptyList())
    val cajas: StateFlow<List<CajaActivaEntity>> = _cajas

    // Estado para saber qué caja seleccionó el usuario
    private val _selectedCajaId = MutableStateFlow<Int?>(null)
    val selectedCajaId: StateFlow<Int?> = _selectedCajaId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeCajas()
        sync()
    }

    private fun observeCajas() {
        viewModelScope.launch {
            cajaRepository.getCajasAbiertas().collect { lista ->
                _cajas.value = lista
                // Si la lista tiene elementos y no hay nada seleccionado, podríamos seleccionar el primero por defecto (opcional)
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