package com.example.sisvvapp.data.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class SyncState {
    IDLE,
    SYNCING,
    PAUSED
}

object SyncCoordinator {
    private val mutex = Mutex()
    private var _state = SyncState.IDLE
    val currentState: SyncState get() = _state

    /**
     * Intenta iniciar una sincronización. Falla si ya hay una en curso o si está pausada.
     */
    suspend fun requestSync(source: String): Boolean {
        mutex.withLock {
            if (_state == SyncState.PAUSED) {
                Log.d(TAG, "Sync PAUSADO globalmente (Usuario en pantalla de cola), ignorando petición de $source")
                return false
            }
            if (_state == SyncState.SYNCING) {
                Log.d(TAG, "Sync ya en curso (solicitado por $source), ignorando")
                return false
            }
            _state = SyncState.SYNCING
            Log.d(TAG, "Sync iniciado por $source")
            return true
        }
    }

    /**
     * Pausa globalmente cualquier intento de sincronización.
     */
    suspend fun pauseSync() {
        mutex.withLock {
            _state = SyncState.PAUSED
            Log.d(TAG, "Sincronización pausada globalmente")
        }
    }

    /**
     * Reanuda el estado de sincronización a IDLE para permitir nuevas peticiones.
     */
    suspend fun resumeSync() {
        mutex.withLock {
            if (_state == SyncState.PAUSED) {
                _state = SyncState.IDLE
                Log.d(TAG, "Sincronización reanudada")
            }
        }
    }

    fun onSyncComplete() {
        // Solo resetear si no fue pausado manualmente durante el proceso
        if (_state == SyncState.SYNCING) {
            _state = SyncState.IDLE
            Log.d(TAG, "Sync completado, estado restablecido a IDLE")
        }
    }

    private const val TAG = "SyncCoordinator"
}
