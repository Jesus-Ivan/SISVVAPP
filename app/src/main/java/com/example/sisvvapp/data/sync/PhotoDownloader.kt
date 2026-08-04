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
import java.security.MessageDigest

object PhotoDownloader {

    private const val TAG = "PhotoDownloader"
    private const val PHOTOS_DIR = "photos"
    private const val MAX_CONCURRENT = 4

    /**
     * Nombre estable y seguro para el archivo local de una foto.
     * Deriva de la URL para evitar path traversal y colisiones, y el mismo
     * valor siempre mapea al mismo archivo (no se re-descarga).
     */
    fun nombreArchivo(url: String): String {
        val cleaned = url.trim()
        val hash = MessageDigest.getInstance("MD5")
            .digest(cleaned.toByteArray())
            .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        return "foto_$hash"
    }

    /**
     * Archivo local en el que debe guardarse / buscarse la foto de la URL dada.
     */
    fun getLocalFile(context: Context, url: String): File =
        File(context.filesDir, "$PHOTOS_DIR/${nombreArchivo(url)}")

    suspend fun downloadAll(context: Context, fotoUrls: List<String>) = withContext(Dispatchers.IO) {
        val uniqueUrls = fotoUrls.filter { !it.isBlank() }.distinct()
        if (uniqueUrls.isEmpty()) return@withContext

        val photosDir = File(context.filesDir, PHOTOS_DIR)
        if (!photosDir.exists()) photosDir.mkdirs()

        val sessionManager = SessionManager.getInstance(context)
        val client = OkHttpClient.Builder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val pending = uniqueUrls.filter { url ->
            !getLocalFile(context, url).exists()
        }

        if (pending.isEmpty()) {
            Log.d(TAG, "Todas las ${uniqueUrls.size} fotos ya están descargadas")
            return@withContext
        }

        Log.d(TAG, "Descargando ${pending.size} fotos de ${uniqueUrls.size} únicas")

        var consecutiveErrors = 0
        val maxConsecutiveErrors = 5

        for (chunk in pending.chunked(MAX_CONCURRENT)) {
            if (consecutiveErrors >= maxConsecutiveErrors) {
                Log.w(TAG, "Demasiados errores consecutivos ($consecutiveErrors), abortando descarga de fotos por este ciclo.")
                break
            }

            val results = chunk.map { url ->
                async {
                    downloadOne(client, sessionManager, context, url)
                }
            }.awaitAll()
            
            // Contar errores en este lote
            val errorsInChunk = results.count { !it }
            if (errorsInChunk > 0) {
                consecutiveErrors += errorsInChunk
            } else {
                consecutiveErrors = 0 // Reset si el lote fue exitoso
            }

            // Retardo entre lotes para no saturar el túnel (loca.lt)
            kotlinx.coroutines.delay(500)
        }

        Log.d(TAG, "Descarga de fotos finalizada (con $consecutiveErrors errores detectados)")
    }

    private fun downloadOne(client: OkHttpClient, sessionManager: SessionManager, context: Context, url: String): Boolean {
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

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body ?: return false
                    val file = getLocalFile(context, url)
                    file.parentFile?.mkdirs()
                    body.byteStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d(TAG, "Foto descargada: $url")
                    return true
                } else {
                    Log.w(TAG, "Error descargando $url: HTTP ${response.code}")
                    return false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Excepción descargando $url: ${e.message}")
            return false
        }
    }
}
