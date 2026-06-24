package com.example.sisvvapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.sisvvapp.BuildConfig
import com.example.sisvvapp.SisvvApplication
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.network.exceptions.ServerUnreachableException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    internal val BASE_URL: String
        get() = BuildConfig.BASE_URL

    private var apiService: ApiService? = null

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun create(context: Context): ApiService {
        if (apiService != null) return apiService!!

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
                    // Hay internet, pero la petición falló -> Servidor caído
                    throw ServerUnreachableException("El servidor no responde: $BASE_URL")
                } else {
                    // No hay internet
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
            
            // Si el código es 429, el túnel está saturado. Cerramos y lanzamos excepción para que no ocupe pool
            if (response.code == 429) {
                response.close()
                throw IOException("Servidor saturado (429)")
            }

            val contentType = response.body?.contentType()?.toString() ?: ""
            
            // Verificamos si existe un token para saber si realmente hay una sesión que pueda expirar
            val hasToken = !sessionManager.getToken().isNullOrEmpty()

            // Solo cerramos sesión en 401 real (no en errores 500+ que devuelvan HTML)
            if (response.code == 401 && hasToken) {
                Log.e("AuthCheck", "¡Sesión expirada detectada! Código: ${response.code}, Tipo: $contentType")
                
                sessionManager.clearSession()
                (context.applicationContext as? SisvvApplication)?.emitUnauthorized()

                // Si el 401 vino como HTML, cerramos y lanzamos excepción para evitar errores de parseo GSON
                if (contentType.contains("text/html")) {
                    response.close()
                    throw IOException("Sesión expirada (Respuesta HTML interceptada)")
                }
            }
            response
        }

        val client = OkHttpClient.Builder()
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

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        return apiService!!
    }
}
