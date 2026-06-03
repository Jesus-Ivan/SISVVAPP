package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.repository.SocioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SociosViewModel(
    private val socioRepository: SocioRepository
) : ViewModel() {

    companion object {
        private const val INITIAL_LIST_LIMIT = 20
    }

    private val _allSocios = socioRepository.getSocios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val socios: StateFlow<List<SocioEntity>> = combine(_allSocios, _searchQuery) { all, query ->
        if (query.isBlank()) {
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _integrantes = MutableStateFlow<List<IntegranteEntity>>(emptyList())
    val integrantes: StateFlow<List<IntegranteEntity>> = _integrantes.asStateFlow()

    private val _selectedSocio = MutableStateFlow<SocioEntity?>(null)
    val selectedSocio: StateFlow<SocioEntity?> = _selectedSocio.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        sync()
    }

    fun search(query: String) {
        _error.value = null
        _searchQuery.value = query
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
            try {
                socioRepository.sync()
                Log.d("SociosVM", "Sync exitoso")
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al sincronizar socios"
                Log.e("SociosVM", "Sync falló", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
