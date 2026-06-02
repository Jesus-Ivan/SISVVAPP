package com.example.sisvvapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.example.sisvvapp.data.local.SessionManager
import okhttp3.OkHttpClient

class SisvvApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val sessionManager = SessionManager.getInstance(this)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Bypass-Tunnel-Reminder", "true")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val token = sessionManager.getToken()
                val request = chain.request().newBuilder()
                    .apply {
                        if (!token.isNullOrEmpty()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}
