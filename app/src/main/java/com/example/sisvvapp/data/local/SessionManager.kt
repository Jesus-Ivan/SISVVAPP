package com.example.sisvvapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.sisvvapp.BuildConfig
import java.io.File
import java.sql.Timestamp

class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al crear EncryptedSharedPreferences, intentando limpiar archivos", e)
            try {
                // Borrado agresivo de las preferencias y archivos relacionados
                context.deleteSharedPreferences(PREFS_NAME)
                
                // Intentar borrar manualmente archivos de keyset que a veces quedan huérfanos
                try {
                    val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
                    if (sharedPrefsDir.exists()) {
                        val keysetFile = File(sharedPrefsDir, "${PREFS_NAME}_keyset.xml")
                        if (keysetFile.exists()) keysetFile.delete()
                    }
                } catch (ioe: Exception) {
                    Log.w("SessionManager", "No se pudo borrar el archivo keyset", ioe)
                }

                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e2: Exception) {
                Log.e("SessionManager", "Error persistente en EncryptedSharedPreferences, usando SharedPreferences normales como fallback", e2)
                // Fallback a SharedPreferences normales si todo falla para evitar el crash fatal
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveToken(token: String) {
        try {
            prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_SESSION_START, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar token", e)
        }
    }

    fun getToken(): String? = try {
        prefs.getString(KEY_TOKEN, null)
    } catch (e: Exception) {
        Log.e("SessionManager", "Error al leer token", e)
        null
    }

    fun saveUserId(id: Int) {
        try {
            prefs.edit().putInt(KEY_USER_ID, id).apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar userId", e)
        }
    }

    fun getUserId(): Int = try {
        prefs.getInt(KEY_USER_ID, -1)
    } catch (e: Exception) {
        Log.e("SessionManager", "Error al leer userId", e)
        -1
    }

    private val sessionKeys = listOf(KEY_TOKEN, KEY_USER_ID, KEY_SESSION_START, KEY_SELECTED_CAJA_ID, KEY_SELECTED_CAJA_NOMBRE, KEY_LAST_SYNC_DATE)

    fun clearSession() {
        try {
            prefs.edit().apply {
                sessionKeys.forEach { remove(it) }
                commit()
            }
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al limpiar sesión", e)
        }
    }

    fun saveThemeMode(mode: Int) {
        try {
            prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar themeMode", e)
        }
    }

    fun getThemeMode(): Int = try {
        prefs.getInt(KEY_THEME_MODE, 0) // 0 = Sistema, 1 = Claro, 2 = Oscuro
    } catch (e: Exception) {
        Log.e("SessionManager", "Error al leer themeMode", e)
        0
    }

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        val sessionStart = prefs.getLong(KEY_SESSION_START, 0L)
        val elapsed = System.currentTimeMillis() - sessionStart
        return elapsed < SESSION_DURATION_MS
    }

    fun getSessionTimeRemainingMs(): Long {
        val sessionStart = prefs.getLong(KEY_SESSION_START, 0L)
        val elapsed = System.currentTimeMillis() - sessionStart
        return (SESSION_DURATION_MS - elapsed).coerceAtLeast(0L)
    }

    fun saveLastSyncDate(timestamp: Long) {
        try {
            prefs.edit().putLong(KEY_LAST_SYNC_DATE, timestamp).apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar lastSyncDate", e)
        }
    }

    fun getLastSyncDate(): Long = try {
        prefs.getLong(KEY_LAST_SYNC_DATE, 0L)
    } catch (e: Exception) {
        Log.e("SessionManager", "Error al leer lastSyncDate", e)
        0L
    }

    fun saveSelectedCaja(id: Int, nombre: String) {
        try {
            prefs.edit()
                .putInt(KEY_SELECTED_CAJA_ID, id)
                .putString(KEY_SELECTED_CAJA_NOMBRE, nombre)
                .apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar caja seleccionada", e)
        }
    }

    fun getSelectedCajaId(): Int = try {
        prefs.getInt(KEY_SELECTED_CAJA_ID, -1)
    } catch (e: Exception) {
        -1
    }

    fun getSelectedCajaNombre(): String = try {
        prefs.getString(KEY_SELECTED_CAJA_NOMBRE, "") ?: ""
    } catch (e: Exception) {
        ""
    }

    fun hasSelectedCaja(): Boolean = getSelectedCajaId() != -1

    fun saveBaseUrl(url: String) {
        try {
            prefs.edit().putString(KEY_BASE_URL, url).apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al guardar baseUrl", e)
        }
    }

    fun getBaseUrl(): String = try {
        prefs.getString(KEY_BASE_URL, BuildConfig.BASE_URL) ?: BuildConfig.BASE_URL
    } catch (e: Exception) {
        Log.e("SessionManager", "Error al leer baseUrl", e)
        BuildConfig.BASE_URL
    }

    fun clearSelectedCaja() {
        try {
            prefs.edit()
                .remove(KEY_SELECTED_CAJA_ID)
                .remove(KEY_SELECTED_CAJA_NOMBRE)
                .apply()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al limpiar caja seleccionada", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        private const val PREFS_NAME = "sisvv_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SESSION_START = "session_start"
        private const val SESSION_DURATION_MS = 12 * 60 * 60 * 1000L // 12 horas

        private const val KEY_SELECTED_CAJA_ID = "selected_caja_id"
        private const val KEY_SELECTED_CAJA_NOMBRE = "selected_caja_nombre"

        private const val KEY_BASE_URL = "base_url"
        private const val KEY_LAST_SYNC_DATE = "last_sync_date"

        fun getInstance(context: Context): SessionManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}