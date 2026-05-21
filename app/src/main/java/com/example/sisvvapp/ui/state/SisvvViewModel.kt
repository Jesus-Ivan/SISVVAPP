package com.example.sisvvapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SisvvViewModel : ViewModel() {

    private val _syncCount = MutableStateFlow(0)
    val syncCount: StateFlow<Int> = _syncCount

    fun refreshSyncCount(database: AppDatabase) {
        viewModelScope.launch {
            _syncCount.value = database.ventaColaDao().countPendientesFlow().first()
        }
    }
}
