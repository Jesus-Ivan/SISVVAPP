package com.example.sisvvapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

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
            prefs.edit().putString(KEY_TOKEN, token).apply()
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

    fun clearSession() {
        // Al limpiar sesión, queremos mantener la preferencia del tema
        val currentTheme = getThemeMode()
        try {
            prefs.edit().clear().commit()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al limpiar sesión", e)
        }
        saveThemeMode(currentTheme)
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

    fun isLoggedIn(): Boolean = getToken() != null

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        private const val PREFS_NAME = "sisvv_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_THEME_MODE = "theme_mode"

        fun getInstance(context: Context): SessionManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}