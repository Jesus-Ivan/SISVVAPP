package com.example.sisvvapp.data.monitor

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitor(private val connectivityManager: ConnectivityManager) {

    val isConnected = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Emitir estado inicial
        val activeNetwork = connectivityManager.activeNetwork
        val initialStatus = activeNetwork != null
        trySend(initialStatus)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.conflate().distinctUntilChanged()
}