package com.example.sisvvapp.data.sync
import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.repository.CajaRepository
import com.example.sisvvapp.data.repository.ProductoRepository
import com.example.sisvvapp.data.repository.SocioRepository
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.RetrofitClient
import java.util.concurrent.TimeUnit
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val api = RetrofitClient.create(applicationContext)
        val socioRepo = SocioRepository(api, db.socioDao())
        val productoRepo = ProductoRepository(api, db.productoDao(), db.grupoModificadorDao())
        val cajaRepo = CajaRepository(api, db.cajaActivaDao())
        val ventaRepo = VentaRepository(api, db.ventaColaDao(), db.ventaRecibidaDao())
        return try {
            // Sync catálogos
            socioRepo.sync()
            productoRepo.sync()
            cajaRepo.sync()
            // Enviar ventas offline pendientes
            val pendientes = ventaRepo.getPendientes()
            for (venta in pendientes) {
                try {
                    ventaRepo.enviarVentaOffline(venta)
                } catch (e: Exception) {
                    Log.w("SyncWorker", "Error al enviar venta ${venta.idTemporal}", e)
                }
            }
            // Guardar timestamp de última sync
            SessionManager.getInstance(applicationContext)
                .saveLastSyncDate(System.currentTimeMillis())
            Log.d("SyncWorker", "Sync completado exitosamente")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error en sync", e)
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
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
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