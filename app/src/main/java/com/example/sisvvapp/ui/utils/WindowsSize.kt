package com.example.sisvvapp.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

enum class DeviceType { MOBILE, TABLET }

@Composable
fun getDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    return if (configuration.smallestScreenWidthDp >= 600) {
        DeviceType.TABLET
    } else {
        DeviceType.MOBILE
    }
}

val LocalDeviceType = staticCompositionLocalOf { DeviceType.MOBILE }

val LocalIsConnected = staticCompositionLocalOf { true }