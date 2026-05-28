@file:kotlin.OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.example.sisvvapp.ui.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
enum class DeviceType { MOBILE, TABLET }

@Composable
fun getDeviceType(): DeviceType {
    val activity = LocalActivity.current ?: return DeviceType.MOBILE

    val windowSizeClass = calculateWindowSizeClass(activity)

    return if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        DeviceType.MOBILE
    } else {
        DeviceType.TABLET
    }
}

val LocalDeviceType = staticCompositionLocalOf { DeviceType.MOBILE }