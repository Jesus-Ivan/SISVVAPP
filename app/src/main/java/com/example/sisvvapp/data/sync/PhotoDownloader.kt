package com.example.sisvvapp.data.sync

import android.content.Context
import android.util.Log
import com.example.sisvvapp.data.local.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

object PhotoDownloader {

    private const val TAG = "PhotoDownloader"
    private const val PHOTOS_DIR = "photos"
    private const val MAX_CONCURRENT = 4

    suspend fun downloadAll(context: Context, fotoUrls: List<String>) = withContext(Dispatchers.IO) {
        val uniqueUrls = fotoUrls.filter { !it.isBlank() }.distinct()
        if (uniqueUrls.isEmpty()) return@withContext

        val photosDir = File(context.filesDir, PHOTOS_DIR)
        if (!photosDir.exists()) photosDir.mkdirs()

        val sessionManager = SessionManager.getInstance(context)
        val client = OkHttpClient.Builder().build()

        val pending = uniqueUrls.filter { url ->
            val file = File(photosDir, url)
            !file.exists()
        }

        if (pending.isEmpty()) {
            Log.d(TAG, "Todas las ${uniqueUrls.size} fotos ya están descargadas")
            return@withContext
        }

        Log.d(TAG, "Descargando ${pending.size} fotos de ${uniqueUrls.size} únicas")

        pending.chunked(MAX_CONCURRENT).forEach { chunk ->
            chunk.map { url ->
                async {
                    downloadOne(client, sessionManager, photosDir, url)
                }
            }.awaitAll()
        }

        Log.d(TAG, "Descarga de fotos completada")
    }

    private fun downloadOne(client: OkHttpClient, sessionManager: SessionManager, photosDir: File, url: String) {
        try {
            val rawBaseUrl = com.example.sisvvapp.network.RetrofitClient.BASE_URL
            val baseUrl = rawBaseUrl
                .trimEnd('/')
                .removeSuffix("/api")
                .removeSuffix("/api/")
            val baseUrlWithSlash = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val relativeUrl = url.trimStart('/')
            val fullUrl = if (url.startsWith("http")) url else "$baseUrlWithSlash$relativeUrl"

            val requestBuilder = Request.Builder().url(fullUrl)
            if (com.example.sisvvapp.BuildConfig.DEBUG) {
                requestBuilder.addHeader("Bypass-Tunnel-Reminder", "true")
            }
            val token = sessionManager.getToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body ?: return
                val file = File(photosDir, url)
                file.parentFile?.mkdirs()
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Foto descargada: $url")
            } else {
                Log.w(TAG, "Error descargando $url: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Excepción descargando $url: ${e.message}")
        }
    }
}
