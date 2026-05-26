package com.example.sisvvapp.network

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.sisvvapp.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://plain-zoos-fail.loca.lt/api/"

    private var apiService: ApiService? = null

    fun create(context: Context): ApiService {
        if (apiService != null) return apiService!!

        val sessionManager = SessionManager.getInstance(context)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val bypassTunnelInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Bypass-Tunnel-Reminder", "true")
                .build()
            chain.proceed(request)
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
            }
            response
        }

        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        val client = OkHttpClient.Builder()
            .addInterceptor(bypassTunnelInterceptor)
            .apply {
                if (isDebug) {
                    addInterceptor(logging)
                }
            }
            .addInterceptor(authInterceptor)
            .addInterceptor(authResponseInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
