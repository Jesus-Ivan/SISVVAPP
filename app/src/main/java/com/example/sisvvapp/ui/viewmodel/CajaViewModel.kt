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
        // Restauramos la selección guardada. -1 = "nunca guardado" → null
        _selectedCajaId.value = sessionManager.getSelectedCajaId().takeIf { it != -1 }
        sync()
    }

    /**
     * Llama al API, actualiza Room, y luego hace una lectura one-shot de la BD
     * para auto-seleccionar la primera caja solo si la selección actual ya no existe.
     * Al usar getCajasSnapshot() DESPUÉS del sync, leemos datos ya estables y evitamos
     * la race condition entre deleteAll() y re-insert del repositorio.
     */
    suspend fun refreshCajas(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        val result = cajaRepository.sync()
        result.onSuccess {
            sessionManager.saveLastSyncDate(System.currentTimeMillis())
            // Leemos la lista estabilizada directamente de Room (one-shot, no Flow)
            val lista = cajaRepository.getCajasSnapshot()
            aplicarAutoSeleccion(lista)
        }
        _isLoading.value = false
        return result.isSuccess
    }

    /**
     * Solo sobreescribe la selección si la actual no existe en la lista recibida.
     * Si el usuario ya eligió una caja válida, no hace nada.
     */
    private fun aplicarAutoSeleccion(lista: List<CajaActivaEntity>) {
        if (lista.isEmpty()) return
        val currentId = _selectedCajaId.value
        val seleccionValida = currentId != null && lista.any { it.id == currentId }
        if (!seleccionValida) {
            val primera = lista.first()
            _selectedCajaId.value = primera.id
            sessionManager.saveSelectedCaja(primera.id, primera.nombre)
            Log.d("CajaVM", "Auto-seleccionando caja: ${primera.nombre} (id=${primera.id})")
        }
    }

    fun selectCaja(id: Int, nombre: String = "") {
        _selectedCajaId.value = id
        sessionManager.saveSelectedCaja(id, nombre)
        Log.d("CajaVM", "Caja seleccionada manualmente: $nombre (id=$id)")
    }

    fun clearSelectedCaja() {
        _selectedCajaId.value = null
        sessionManager.saveSelectedCaja(-1, "")
        Log.d("CajaVM", "Selección de caja limpiada")
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = cajaRepository.sync()
                result.fold(
                    onSuccess = {
                        sessionManager.saveLastSyncDate(System.currentTimeMillis())
                        // También aplicamos auto-selección en el sync inicial del init
                        val lista = cajaRepository.getCajasSnapshot()
                        aplicarAutoSeleccion(lista)
                    },
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
