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
import com.example.sisvvapp.R
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
    private val context: Context  // Siempre recibe applicationContext (ver factory)
) : ViewModel() {

    private val api: ApiService = RetrofitClient.create(context)
    private val sessionManager = SessionManager.getInstance(context)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

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

    var logoutSuccess by mutableStateOf(false)
        private set

    // ── Theme State ─────────────────────────────────────────────────────────

    var themeMode by mutableStateOf(sessionManager.getThemeMode())
        private set

    fun updateThemeMode(mode: Int) {
        themeMode = mode
        sessionManager.saveThemeMode(mode)
    }

    // ── Network State ───────────────────────────────────────────────────────

    var isOnline by mutableStateOf(isNetworkAvailable())
        private set

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                isOnline = true
            }

            override fun onLost(network: android.net.Network) {
                isOnline = false
            }
        }
        val networkRequest = android.net.NetworkRequest.Builder()
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    override fun onCleared() {
        super.onCleared()
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback?.let { cm.unregisterNetworkCallback(it) }
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
                sessionManager.clearSelectedCaja() // Limpiar también la caja al cerrar sesión
                loginSuccess = false
                loginError = null
                logoutSuccess = true
                try {
                    WorkManager.getInstance(context).cancelAllWork()
                } catch (e: Exception) {
                    Log.e("LOGOUT", "Error al cancelar WorkManager", e)
                }
            }
        }
    }

    fun resetLogoutStatus() {
        logoutSuccess = false
    }

    fun resetLoginStatus() {
        loginSuccess = false
        loginError = null
    }

    // ── Login Modificado ───────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            loginError = null
            networkError = null

            if (!isNetworkAvailable()) {
                networkError = context.getString(R.string.error_no_internet)
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
                    when (response.code()) {
                        401, 400 -> {
                            loginError = context.getString(R.string.credenciales_incorrectas)
                        }
                        500, 502, 503, 504 -> {
                            networkError = context.getString(R.string.error_servidor_caido)
                        }
                        else -> {
                            networkError = context.getString(R.string.error_inesperado)
                        }
                    }
                    Log.e("LOGIN", "Error HTTP Código: ${response.code()}")
                }
            } catch (e: Exception) {
                networkError = when (e) {
                    is UnknownHostException -> context.getString(R.string.error_no_internet)
                    is ConnectException,
                    is SocketTimeoutException,
                    is SSLException -> context.getString(R.string.error_servidor_caido)
                    else -> context.getString(R.string.error_inesperado)
                }
                Log.e("LOGIN", "Exception de conexión física", e)
            } finally {
                isLoading = false
            }
        }
    }
}