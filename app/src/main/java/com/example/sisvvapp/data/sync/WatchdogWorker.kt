package com.example.sisvvapp.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
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
            } else {
                Log.d(TAG, "Watchdog: sin ventas pendientes")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Watchdog error", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sisvv_watchdog"
        private const val INTERVAL_MINUTES = 2L
        const val TAG = "WatchdogWorker"

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WatchdogWorker>(INTERVAL_MINUTES, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
