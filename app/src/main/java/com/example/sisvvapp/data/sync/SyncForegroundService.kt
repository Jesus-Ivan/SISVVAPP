package com.example.sisvvapp.data.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SyncForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var pendingCountJob: Job? = null

    companion object {
        private const val STOP_COOLDOWN_MS = 30_000L
        const val CHANNEL_ID = "sync_foreground_channel"
        const val NOTIFICATION_ID = 202
        const val TAG = "SyncForegroundService"

        fun start(context: Context) {
            try {
                val intent = Intent(context, SyncForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "No se puede iniciar SyncForegroundService desde background: ${e.message}")
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SyncForegroundService::class.java))
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Sincronización SISVV",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notificación de sincronización de ventas"
                    setSound(null, null)
                    enableVibration(false)
                    setShowBadge(false)
                }
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()
        startMonitoring()
        Log.d(TAG, "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(0))
        triggerSync()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        pendingCountJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "Servicio destruido")
        super.onDestroy()
    }

    private fun registerNetworkCallback() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Red disponible")
                triggerSync()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Red perdida")
            }
        }

        val request = NetworkRequest.Builder()
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        networkCallback = callback
    }

    private fun triggerSync() {
        serviceScope.launch {
            if (!SyncCoordinator.requestSync("SyncForegroundService")) return@launch

            try {
                delay(2000)
                val db = AppDatabase.getInstance(this@SyncForegroundService)
                val pendientes = db.ventaColaDao().getParaSincronizar()
                if (pendientes.isEmpty()) {
                    Log.d(TAG, "Sin ventas pendientes, no se dispara sync")
                    return@launch
                }

                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                val hayRed = activeNetwork != null && capabilities != null

                if (hayRed) {
                    Log.d(TAG, "Red disponible, sync directo (${pendientes.size} pendientes)")
                    val api = RetrofitClient.create(this@SyncForegroundService)
                    val ventaRepo = VentaRepository(api, db, this@SyncForegroundService)
                    ventaRepo.syncVentas(java.time.LocalDate.now().toString())

                    for (venta in pendientes) {
                        try {
                            ventaRepo.enviarVentaOffline(venta)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error enviando venta ${venta.idTemporal}", e)
                        }
                    }
                } else {
                    Log.d(TAG, "Sin red disponible, delegando a WorkManager para Doze")
                    SyncWorker.enqueueOneTime(this@SyncForegroundService)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en sync", e)
            } finally {
                SyncCoordinator.onSyncComplete()
            }
        }
    }

    private fun startMonitoring() {
        pendingCountJob = serviceScope.launch {
            val db = AppDatabase.getInstance(this@SyncForegroundService)
            db.ventaColaDao().countAllPendientesFlow().collect { count ->
                Log.d(TAG, "Ventas en cola: $count")
                val notification = buildNotification(count)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, notification)

                if (count == 0) {
                    delay(STOP_COOLDOWN_MS)
                    val finalCount = db.ventaColaDao().getParaSincronizar().size
                    if (finalCount == 0) {
                        Log.d(TAG, "Cola vacía durante $STOP_COOLDOWN_MS ms, deteniendo servicio")
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun buildNotification(pendingCount: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SISVV sincronizando")
            .setContentText("Ventas pendientes: $pendingCount")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }
}
