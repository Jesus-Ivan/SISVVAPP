package com.example.sisvvapp.data.sync

import android.content.Context
import androidx.work.*
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.RetrofitClient
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val ventaRepository = VentaRepository(
            RetrofitClient.create(applicationContext),
            db.ventaColaDao()
        )

        return try {
            val pendientes = ventaRepository.getPendientes()
            if (pendientes.isEmpty()) return Result.success()

            for (venta in pendientes) {
                ventaRepository.enviarVentaOffline(venta)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sisvv_sync_worker"

        /** Enqueue a one-time sync request triggered when going back online. */
        fun enqueue(context: Context) {
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
    }
}
