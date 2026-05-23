package com.example.sisvvapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserId(id: Int) {
        prefs.edit().putInt(KEY_USER_ID, id).apply()
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun clearSession() {
        prefs.edit().clear().commit()
    }

    fun isLoggedIn(): Boolean = getToken() != null

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        private const val PREFS_NAME = "sisvv_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"

        fun getInstance(context: Context): SessionManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}