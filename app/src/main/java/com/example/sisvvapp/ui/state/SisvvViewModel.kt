package com.example.sisvvapp.ui.state

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.network.ApiService
import com.example.sisvvapp.network.RetrofitClient
import com.example.sisvvapp.network.dto.auth.LoginRequest
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class SisvvViewModel(
    private val context: Context
) : ViewModel() {

    private val api: ApiService = RetrofitClient.create(context)
    private val sessionManager = SessionManager.getInstance(context)

    // ── Network check ──────────────────────────────────────────────────────

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ── Auth state ──────────────────────────────────────────────────────────

    var isLoading by mutableStateOf(false)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    var networkError by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    // ── Theme State ─────────────────────────────────────────────────────────

    var themeMode by mutableStateOf(sessionManager.getThemeMode())
        private set

    fun updateThemeMode(mode: Int) {
        themeMode = mode
        sessionManager.saveThemeMode(mode)
    }

    // ── Logout ──────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            try {
                if (isNetworkAvailable()) {
                    api.logout()
                }
            } catch (e: Exception) {
                Log.e("LOGOUT", "Error al llamar logout API", e)
            } finally {
                sessionManager.clearSession()
                loginSuccess = false
                loginError = null
                try {
                    WorkManager.getInstance(context).cancelAllWork()
                } catch (e: Exception) {
                    Log.e("LOGOUT", "Error al cancelar WorkManager", e)
                }
            }
        }
    }

    // ── Login ───────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            loginError = null
            networkError = null

            if (!isNetworkAvailable()) {
                networkError = "No hay conexión a internet. Verifica tu red e intenta de nuevo"
                isLoading = false
                return@launch
            }
            try {
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        sessionManager.saveToken(body.token)
                        sessionManager.saveUserId(body.user.id)

                        Log.d("LOGIN", "Token: ${body.token}")
                        loginSuccess = true
                    }
                } else {
                    loginError = "Credenciales Incorrectas"
                    Log.e("LOGIN", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                networkError = when (e) {
                    is UnknownHostException,
                    is ConnectException,
                    is SocketTimeoutException,
                    is SSLException ->
                        "No hay conexión a internet. Verifica tu red e intenta de nuevo."

                    else -> "Ocurrió un error inesperado. Intenta de nuevo"
                }
                Log.e("LOGIN", "Exeption", e)
            } finally {
                isLoading = false
            }
        }
    }
}
