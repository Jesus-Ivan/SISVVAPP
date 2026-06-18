package com.example.sisvvapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.example.sisvvapp.data.local.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient

import com.example.sisvvapp.data.sync.SyncForegroundService
import com.example.sisvvapp.data.sync.SyncWorker
import com.example.sisvvapp.data.sync.WatchdogWorker

class SisvvApplication : Application(), ImageLoaderFactory {

    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvent: SharedFlow<Unit> = _unauthorizedEvent

    override fun onCreate() {
        super.onCreate()
        // Configurar sincronización periódica en segundo plano
        SyncWorker.enqueuePeriodic(this)

        // Iniciar foreground service para monitoreo de red en tiempo real
        SyncForegroundService.start(this)

        // Watchdog para recuperar servicio después de force kill
        WatchdogWorker.enqueue(this)
    }

    fun emitUnauthorized() {
        _unauthorizedEvent.tryEmit(Unit)
    }
    override fun newImageLoader(): ImageLoader {
        val sessionManager = SessionManager.getInstance(this)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                var builder = chain.request().newBuilder()
                if (BuildConfig.DEBUG) {
                    builder = builder.addHeader("Bypass-Tunnel-Reminder", "true")
                }
                chain.proceed(builder.build())
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
