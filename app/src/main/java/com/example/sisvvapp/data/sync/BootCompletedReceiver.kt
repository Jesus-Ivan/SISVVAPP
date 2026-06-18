package com.example.sisvvapp.data.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sisvvapp.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Dispositivo encendido. Verificando comandas pendientes...")
            val appContext = context.applicationContext
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(appContext)
                    val pendientes = db.ventaColaDao().getParaSincronizar()
                    if (pendientes.isNotEmpty()) {
                        Log.d("BootReceiver", "Se encontraron ${pendientes.size} comandas pendientes. Reactivando sincronización.")
                        
                        // Arrancar SyncWorker para sincronización en background inmediata
                        SyncWorker.enqueueOneTime(appContext)
                        
                        // Iniciar SyncForegroundService (exención de inicio desde background permitida bajo BOOT_COMPLETED)
                        SyncForegroundService.start(appContext)
                    } else {
                        Log.d("BootReceiver", "Sin comandas pendientes en el arranque.")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error al procesar el encendido", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
