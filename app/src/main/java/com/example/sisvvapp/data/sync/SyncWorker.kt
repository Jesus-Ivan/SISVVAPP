package com.example.sisvvapp.data.sync
import android.content.Context
import android.util.Log
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

            // 4. Verificar cola y detener foreground service si está vacía
            val restantes = ventaRepo.getParaSincronizar()
            if (restantes.isEmpty()) {
                Log.d("SyncWorker", "Cola vacía, deteniendo foreground service")
                SyncForegroundService.stop(applicationContext)
            } else {
                SyncForegroundService.start(applicationContext)
            }

            // 5. Finalizar
            if (envioExitoso) {
                SessionManager.getInstance(applicationContext)
                    .saveLastSyncDate(System.currentTimeMillis())
                Log.d("SyncWorker", "Sync completado exitosamente")
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error general en sync", e)
            Result.retry()
        }
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
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
