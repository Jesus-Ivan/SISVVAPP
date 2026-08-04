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
        if (!SyncCoordinator.requestSync("SyncWorker")) {
            Log.d("SyncWorker", "Sync ya en curso por otro componente, saltando")
            return Result.success()
        }

        return try {
            delay(1000)
            val db = AppDatabase.getInstance(applicationContext)
            val api = RetrofitClient.create(applicationContext)
            val socioRepo = SocioRepository(api, db, db.socioDao(), applicationContext)
            val productoRepo = ProductoRepository(api, db, db.productoDao(), db.grupoModificadorDao())
            val cajaRepo = CajaRepository(api, db, db.cajaActivaDao())
            val tipoPagoRepo = TipoPagoRepository(api, db, db.tipoPagoDao())
            val tipoVentaRepo = com.example.sisvvapp.data.repository.TipoVentaRepository(api, db, db.tipoVentaDao())
            val ventaRepo = VentaRepository(api, db, applicationContext)

            val syncCatalogs = inputData.getBoolean("sync_catalogs", false)

            if (syncCatalogs) {
                socioRepo.sync()
                productoRepo.sync()
                cajaRepo.sync()
                tipoPagoRepo.sync()
                tipoVentaRepo.sync()
            }

            ventaRepo.syncVentas(java.time.LocalDate.now().toString())

            // Descargar fotos de forma limitada para no saturar el túnel
            try {
                val socios = db.socioDao().getAllSociosSync()
                val integrantes = db.socioDao().getAllIntegrantesSync()
                val fotoUrls = (socios.mapNotNull { it.fotoUrl } + integrantes.mapNotNull { it.fotoUrl })
                    .filter { it.isNotBlank() }
                    .distinct()
                
                val limit = 40
                val pending = fotoUrls.filter { !it.isBlank() && !PhotoDownloader.getLocalFile(applicationContext, it).exists() }.take(limit)
                
                if (pending.isNotEmpty()) {
                    Log.d("SyncWorker", "Descarga gradual: ${pending.size} fotos pendientes de bajar en este ciclo")
                    PhotoDownloader.downloadAll(applicationContext, pending)
                }
            } catch (e: Exception) {
                Log.w("SyncWorker", "Error descargando fotos (sync parcial)", e)
            }

            // Descargar imágenes de productos de forma limitada
            try {
                val productoImgUrls = db.productoDao().getAllProductosImagenes()
                    .filter { !it.isNullOrBlank() }
                    .map { it!! }
                    .distinct()

                val limit = 20
                val pendingProductos = productoImgUrls.filter { it.isNotBlank() && !PhotoDownloader.getLocalFile(applicationContext, it).exists() }.take(limit)

                if (pendingProductos.isNotEmpty()) {
                    Log.d("SyncWorker", "Descarga gradual: ${pendingProductos.size} imágenes de productos pendientes")
                    PhotoDownloader.downloadAll(applicationContext, pendingProductos)
                }
            } catch (e: Exception) {
                Log.w("SyncWorker", "Error descargando imágenes de productos (sync parcial)", e)
            }

            // Enviar ventas offline
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

            // Verificar cola y detener foreground service si está vacía
            val restantes = ventaRepo.getParaSincronizar()
            if (restantes.isEmpty()) {
                Log.d("SyncWorker", "Cola vacía, deteniendo foreground service")
                SyncForegroundService.stop(applicationContext)
            } else {
                SyncForegroundService.start(applicationContext)
            }

            // Finalizar
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
        } finally {
            SyncCoordinator.onSyncComplete()
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
                .setInputData(workDataOf("sync_catalogs" to true))
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
                .setInputData(workDataOf("sync_catalogs" to true))
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
