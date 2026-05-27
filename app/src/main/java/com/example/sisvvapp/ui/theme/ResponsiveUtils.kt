package com.example.sisvvapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

val LocalScaleFactor = staticCompositionLocalOf { 1f }

@Composable
fun rememberScaleFactor(): Float {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return (widthDp / 360f).coerceIn(1f, 2f)
}
