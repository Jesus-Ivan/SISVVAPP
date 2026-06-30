package com.example.sisvvapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.data.sync.SyncCoordinator
import com.example.sisvvapp.data.sync.SyncForegroundService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VentasPendientesViewModel(
    private val ventaRepository: VentaRepository,
    private val context: Context
) : ViewModel() {

    val ventasPendientes: StateFlow<List<VentaColaEntity>> = ventaRepository.getParaSincronizarFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Pausa la sincronización proactivamente al entrar a la cola.
     * Esto evita condiciones de carrera entre el usuario descartando y el worker enviando.
     */
    fun pausarSincronizacion() {
        SyncForegroundService.stop(context)
        viewModelScope.launch {
            SyncCoordinator.pauseSync()
        }
    }

    /**
     * Reanuda la sincronización al salir de la pantalla.
     */
    fun reanudarSincronizacion() {
        viewModelScope.launch {
            SyncCoordinator.resumeSync()
            val pendientes = ventaRepository.getParaSincronizarFlow().first()
            if (pendientes.isNotEmpty()) {
                SyncForegroundService.start(context)
            }
        }
    }

    fun descartarVenta(idTemporal: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = ventaRepository.descartarVentaPendiente(idTemporal)
            onResult(result)
        }
    }
}
