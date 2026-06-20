package com.example.sisvvapp.data.sync

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SyncEventBus {
    private val _events = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SyncEvent> = _events.asSharedFlow()

    fun cajaCerrada(ventaId: String) {
        _events.tryEmit(SyncEvent.CajaCerrada(ventaId))
    }

    sealed class SyncEvent {
        data class CajaCerrada(val ventaId: String) : SyncEvent()
    }
}
