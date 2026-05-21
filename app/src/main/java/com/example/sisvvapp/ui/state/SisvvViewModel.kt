package com.example.sisvvapp.ui.state

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.AppDatabase
import com.sisvv.mobile.network.RetrofitClient
import com.sisvv.mobile.network.ApiService
import com.sisvv.mobile.network.dto.auth.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SisvvViewModel(
    private val context: Context
) : ViewModel() {

    private val api: ApiService = RetrofitClient.create(context)
    private val db: AppDatabase = AppDatabase.getInstance(context)

    // ── Sync count ─────────────────────────────────────────────────────────

    private val _syncCount = MutableStateFlow(0)
    val syncCount: StateFlow<Int> = _syncCount

    fun refreshSyncCount() {
        viewModelScope.launch {
            _syncCount.value = db.ventaColaDao().countPendientesFlow().first()
        }
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

    // ── Login ───────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            loginError = null
            networkError = null

            try {
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val prefs = context.getSharedPreferences(
                            "sisvv_prefs",
                            Context.MODE_PRIVATE
                        )
                        prefs.edit()
                            .putString("token", body.token)
                            .apply()

                        Log.d("LOGIN", "Token: ${body.token}")
                        loginSuccess = true
                    }
                } else {
                    loginError = "Credenciales incorrectas"
                    Log.e("LOGIN", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                networkError = e.message
                Log.e("LOGIN", "Exception", e)
            } finally {
                isLoading = false
            }
        }
    }
}
