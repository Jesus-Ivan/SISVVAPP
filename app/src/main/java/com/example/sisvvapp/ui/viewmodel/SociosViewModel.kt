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

    companion object {
        private const val INITIAL_LIST_LIMIT = 20
    }

    private val _allSocios = MutableStateFlow<List<SocioEntity>>(emptyList())

    private val _socios = MutableStateFlow<List<SocioEntity>>(emptyList())
    val socios: StateFlow<List<SocioEntity>> = _socios

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _integrantes = MutableStateFlow<List<IntegranteEntity>>(emptyList())
    val integrantes: StateFlow<List<IntegranteEntity>> = _integrantes.asStateFlow()

    private val _selectedSocio = MutableStateFlow<SocioEntity?>(null)
    val selectedSocio: StateFlow<SocioEntity?> = _selectedSocio.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeSocios()
        sync()
    }

    private fun observeSocios() {
        viewModelScope.launch {
            socioRepository.getSocios().collect { lista ->
                _allSocios.value = lista
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val query = _searchQuery.value
        val all = _allSocios.value
        _socios.value = if (query.isBlank()) {
            all.take(INITIAL_LIST_LIMIT)
        } else {
            val q = query.lowercase()
            all.filter { socio ->
                socio.id.toString().contains(q) ||
                    socio.nombre.lowercase().contains(q) ||
                    socio.apellidoP.lowercase().contains(q) ||
                    socio.apellidoM.lowercase().contains(q)
            }
        }
    }

    fun search(query: String) {
        _error.value = null
        _searchQuery.value = query
        applyFilter()
    }

    fun getIntegrantesPorSocio(socioId: Int) {
        viewModelScope.launch {
            val result = socioRepository.getSocioConIntegrantes(socioId)
            Log.d("SociosVM", "getIntegrantesPorSocio($socioId) → ${result?.integrantes?.size} integrantes, socio=${result?.socio?.nombre}")
            if (result != null) {
                _selectedSocio.value = result.socio
                _integrantes.value = result.integrantes
            } else {
                val socio = socioRepository.getSocioById(socioId)
                if (socio != null) {
                    _selectedSocio.value = socio
                }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = socioRepository.sync()
            result.fold(
                onSuccess = { Log.d("SociosVM", "Sync exitoso") },
                onFailure = { e ->
                    _error.value = e.message ?: "Error al sincronizar socios"
                    Log.e("SociosVM", "Sync falló", e)
                }
            )
            _isLoading.value = false
        }
    }
}
