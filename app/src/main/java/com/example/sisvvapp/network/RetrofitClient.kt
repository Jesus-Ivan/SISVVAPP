package com.example.sisvvapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
                    throw ServerUnreachableException("El servidor no responde (loca.lt)")
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
            if (response.code == 401) {
                sessionManager.clearSession()
                (context.applicationContext as? SisvvApplication)?.emitUnauthorized()
            }
            response
        }

        val client = OkHttpClient.Builder()
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
