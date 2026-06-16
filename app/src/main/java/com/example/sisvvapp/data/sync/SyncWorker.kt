package com.example.sisvvapp.data.sync
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.repository.CajaRepository
import com.example.sisvvapp.data.repository.ProductoRepository
import com.example.sisvvapp.data.repository.SocioRepository
import com.example.sisvvapp.data.repository.TipoPagoRepository
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.RetrofitClient
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        if (runAttemptCount > 5) return Result.failure()
        

        try {
            setForeground(getForegroundInfo())
        } catch (e: Exception) {
            Log.w("SyncWorker", "No se pudo establecer primer plano, continuando normal")
        }

        delay(1000)
        val db = AppDatabase.getInstance(applicationContext)
        val api = RetrofitClient.create(applicationContext)
        val socioRepo = SocioRepository(api, db.socioDao(), applicationContext)
        val productoRepo = ProductoRepository(api, db.productoDao(), db.grupoModificadorDao())
        val cajaRepo = CajaRepository(api, db.cajaActivaDao())
        val tipoPagoRepo = TipoPagoRepository(api, db.tipoPagoDao())
        val tipoVentaRepo = com.example.sisvvapp.data.repository.TipoVentaRepository(api, db.tipoVentaDao())
        val ventaRepo = VentaRepository(api, db, applicationContext)
        return try {
            // Sync catálogos
            socioRepo.sync()
            productoRepo.sync()
            cajaRepo.sync()
            tipoPagoRepo.sync()
            tipoVentaRepo.sync()
            ventaRepo.syncVentas(java.time.LocalDate.now().toString())

            // Sincronizar ventas del día y descargar detalles (Prefetch)
            val today = java.time.LocalDate.now().toString()
            ventaRepo.syncVentas(today)

            // Descargar fotos de socios e integrantes para uso offline
            try {
                val socios = db.socioDao().getAllSociosSync()
                val integrantes = db.socioDao().getAllIntegrantesSync()
                val fotoUrls = (socios.mapNotNull { it.fotoUrl } + integrantes.mapNotNull { it.fotoUrl })
                    .filter { it.isNotBlank() }
                PhotoDownloader.downloadAll(applicationContext, fotoUrls)
            } catch (e: Exception) {
                Log.w("SyncWorker", "Error descargando fotos", e)
            }
            // 3. Enviar ventas offline
            val pendientes = ventaRepo.getParaSincronizar()
            var envioExitoso = true
            for (venta in pendientes) {
                try {
                    if (ventaRepo.enviarVentaOffline(venta).isFailure) {
                        envioExitoso = false
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error crítico en venta ${venta.idTemporal}", e)
                    envioExitoso = false
                }
            }

            // 4. Finalizar
            if (envioExitoso) {
                SessionManager.getInstance(applicationContext)
                    .saveLastSyncDate(System.currentTimeMillis())
                Log.d("SyncWorker", "Sync completado exitosamente")
                Result.success()
            } else {
                Result.retry() // Retentará con política exponencial configurada en companion
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error general en sync", e)
            Result.retry()
        }
    }
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val channelId = "sync_channel"
        val notificationId = 101

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronización de Datos"
            val descriptionText = "Mantiene las ventas sincronizadas con el servidor"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("SISVV: Sincronizando")
            .setContentText("Enviando ventas pendientes...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return ForegroundInfo(
            notificationId,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )
    }

    companion object {
        const val WORK_NAME = "sisvv_sync_worker"
        const val PERIODIC_WORK_NAME = "sisvv_sync_periodic"
        fun enqueueOneTime(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES // Frecuencia mínima permitida por Android
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, // Mantener el existente para no resetear el cronómetro
                    request
                )
        }
    }
}