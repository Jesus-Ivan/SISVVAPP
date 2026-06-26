package com.example.sisvvapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.sisvvapp.BuildConfig
import com.example.sisvvapp.SisvvApplication
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.network.exceptions.ServerUnreachableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    internal var BASE_URL: String = BuildConfig.BASE_URL
        private set

    private var okHttpClient: OkHttpClient? = null

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getOrBuildOkHttpClient(context: Context): OkHttpClient {
        if (okHttpClient == null) {
            val sessionManager = SessionManager.getInstance(context)

            val jsonAcceptInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .build()
                chain.proceed(request)
            }

            val bypassTunnelInterceptor = Interceptor { chain ->
                var builder = chain.request().newBuilder()
                if (BuildConfig.DEBUG) {
                    builder = builder.addHeader("Bypass-Tunnel-Reminder", "true")
                }
                chain.proceed(builder.build())
            }

            val connectivityInterceptor = Interceptor { chain ->
                try {
                    val response = chain.proceed(chain.request())
                    response
                } catch (e: IOException) {
                    if (isNetworkAvailable(context)) {
                        throw ServerUnreachableException("El servidor no responde: $BASE_URL")
                    } else {
                        throw e
                    }
                }
            }

            val authInterceptor = Interceptor { chain ->
                val token = sessionManager.getToken()
                val request = chain.request()
                    .newBuilder()
                    .apply {
                        if (!token.isNullOrEmpty()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }
                    .build()
                chain.proceed(request)
            }

            val authResponseInterceptor = Interceptor { chain ->
                val response = chain.proceed(chain.request())

                if (response.code == 429) {
                    response.close()
                    throw IOException("Servidor saturado (429)")
                }

                val contentType = response.body?.contentType()?.toString() ?: ""
                val hasToken = !sessionManager.getToken().isNullOrEmpty()

                if (response.code == 401 && hasToken) {
                    Log.e("AuthCheck", "Sesion expirada detectada! Codigo: ${response.code}, Tipo: $contentType")
                    sessionManager.clearSession()
                    (context.applicationContext as? SisvvApplication)?.emitUnauthorized()
                    if (contentType.contains("text/html")) {
                        response.close()
                        throw IOException("Sesion expirada (Respuesta HTML interceptada)")
                    }
                }
                response
            }

            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(jsonAcceptInterceptor)
                .addInterceptor(bypassTunnelInterceptor)
                .addInterceptor(connectivityInterceptor)
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                    }
                }
                .addInterceptor(authInterceptor)
                .addInterceptor(authResponseInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
        }
        return okHttpClient!!
    }

    fun create(context: Context): ApiService {
        val sessionManager = SessionManager.getInstance(context)
        BASE_URL = sessionManager.getBaseUrl()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOrBuildOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    fun updateBaseUrl(context: Context, newUrl: String) {
        val sessionManager = SessionManager.getInstance(context)
        sessionManager.saveBaseUrl(newUrl)
        BASE_URL = newUrl
    }

    suspend fun testConnection(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val testUrl = if (url.endsWith("/")) url else "$url/"
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .followRedirects(false)
                .build()
            val request = Request.Builder()
                .url(testUrl)
                .method("GET", null)
                .addHeader("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val code = response.code
            response.close()

            if (code >= 500) {
                return@withContext Result.failure(Exception("HTTP $code — Error interno del servidor"))
            }
            if (code == 429) {
                return@withContext Result.failure(Exception("HTTP 429 — Servidor saturado, intente más tarde"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
