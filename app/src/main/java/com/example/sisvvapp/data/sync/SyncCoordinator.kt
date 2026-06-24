package com.example.sisvvapp.data.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class SyncState {
    IDLE,
    SYNCING
}

object SyncCoordinator {
    private val mutex = Mutex()
    private var _state = SyncState.IDLE
    val currentState: SyncState get() = _state

    suspend fun requestSync(source: String): Boolean {
        mutex.withLock {
            if (_state != SyncState.IDLE) {
                Log.d(TAG, "Sync ya en curso (solicitado por $source), ignorando")
                return false
            }
            _state = SyncState.SYNCING
            Log.d(TAG, "Sync iniciado por $source")
            return true
        }
    }

    fun onSyncComplete() {
        _state = SyncState.IDLE
        Log.d(TAG, "Sync completado, estado restablecido a IDLE")
    }

    private const val TAG = "SyncCoordinator"
}
