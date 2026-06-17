package com.example.sisvvapp.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.sisvvapp.data.local.AppDatabase
import java.util.concurrent.TimeUnit

class WatchdogWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val pendientes = db.ventaColaDao().getParaSincronizar()

            if (pendientes.isNotEmpty()) {
                Log.d(TAG, "Watchdog: ${pendientes.size} ventas pendientes, reiniciando servicio")
                SyncForegroundService.start(applicationContext)
                SyncWorker.enqueueOneTime(applicationContext)
                enqueue(applicationContext)
            } else {
                Log.d(TAG, "Watchdog: sin ventas pendientes, deteniendo monitoreo")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Watchdog error", e)
            enqueue(applicationContext)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sisvv_watchdog"
        private const val INTERVAL_MINUTES = 2L
        const val TAG = "WatchdogWorker"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<WatchdogWorker>()
                .setInitialDelay(INTERVAL_MINUTES, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
