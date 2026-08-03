package com.example.sisvvapp.data.sync

import android.content.Context
import android.util.Log
import com.example.sisvvapp.data.local.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

object ManualDownloader {

    private const val TAG = "ManualDownloader"
    private const val MANUAL_DIR = "manual"
    private const val FILE_NAME = "manual_usuario.pdf"

    fun getManualFile(context: Context): File =
        File(context.filesDir, "$MANUAL_DIR/$FILE_NAME")

    fun isDownloaded(context: Context): Boolean = getManualFile(context).exists()

    /**
     * Descarga el PDF del manual desde GET /api/manual. Devuelve true si se
     * guardó (o reemplazó) correctamente.
     */
    suspend fun download(context: Context): Boolean = withContext(Dispatchers.IO) {
        val sessionManager = SessionManager.getInstance(context)
        val client = OkHttpClient.Builder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val baseUrl = com.example.sisvvapp.network.RetrofitClient.BASE_URL.trimEnd('/')
        val fullUrl = "$baseUrl/manual"

        val requestBuilder = Request.Builder().url(fullUrl)
        if (com.example.sisvvapp.BuildConfig.DEBUG) {
            requestBuilder.addHeader("Bypass-Tunnel-Reminder", "true")
        }
        val token = sessionManager.getToken()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Error descargando manual: HTTP ${response.code}")
                    return@withContext false
                }
                val body = response.body ?: return@withContext false
                val file = getManualFile(context)
                file.parentFile?.mkdirs()
                body.byteStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                Log.d(TAG, "Manual descargado: ${file.absolutePath}")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Excepción descargando manual: ${e.message}")
            return@withContext false
        }
    }
}
