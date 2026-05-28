package com.example.sisvvapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _destination = MutableSharedFlow<String>()
    val destination = _destination.asSharedFlow()

    init {
        decideNextDestination()
    }

    private fun decideNextDestination() {
        viewModelScope.launch {
            // Esperamos lo que dura la animación principal (aprox 3 seg)
            delay(3000)

            val route = if (sessionManager.isLoggedIn()) {
                Screen.CajaInicial.route
            } else {
                Screen.Login.route
            }

            _destination.emit(route)
        }
    }
}