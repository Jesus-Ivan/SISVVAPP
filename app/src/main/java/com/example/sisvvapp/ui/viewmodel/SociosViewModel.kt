package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.repository.SocioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SociosViewModel(
    private val socioRepository: SocioRepository
) : ViewModel() {

    private val _socios = MutableStateFlow<List<SocioEntity>>(emptyList())
    val socios: StateFlow<List<SocioEntity>> = _socios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _integrantes = MutableStateFlow<List<IntegranteEntity>>(emptyList())
    val integrantes: StateFlow<List<IntegranteEntity>> = _integrantes.asStateFlow()

    init {
        observeSocios()
        sync()
    }

    private fun observeSocios() {
        viewModelScope.launch {
            socioRepository.getSocios().collect { lista ->
                _socios.value = lista
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            socioRepository.searchSocios(query).collect { lista ->
                _socios.value = lista
            }
        }
    }

    fun getIntegrantesPorSocio(socioId: Int) {
        viewModelScope.launch {
        }
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                socioRepository.sync()
            } catch (e: Exception) {
                Log.e("SociosVM", "Sync falló", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
