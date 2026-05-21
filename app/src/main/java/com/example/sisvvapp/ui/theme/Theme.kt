package com.example.sisvvapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Spotify Dark Color Scheme ───────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = EcoGreenPrimary,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFF123F2E),
    onPrimaryContainer = Color.White,
    secondary          = EcoGreenSecondary,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF282828),
    onSecondaryContainer = Color.White,
    tertiary           = EcoGreenPrimary,
    onTertiary         = Color.White,
    background         = Color(0xFF121212),   // Spotify style background
    onBackground       = Color(0xFFECEFF1),
    surface            = Color(0xFF181818),   // Spotify card surface
    onSurface          = Color(0xFFECEFF1),
    surfaceVariant     = Color(0xFF282828),
    onSurfaceVariant   = Color(0xFFB3B3B3),
    outline            = Color(0xFF2E2E2E),
    error              = StatusCancelled,
)

// ─── Eco-Minimal Light Color Scheme ──────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = EcoGreenPrimary,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFE6F7F0),   // Soft Jade container
    onPrimaryContainer = EcoGreenSecondary,
    secondary          = EcoGreenSecondary,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFF1F3F5),
    onSecondaryContainer = EcoTextHigh,
    tertiary           = EcoGreenSecondary,
    onTertiary         = Color.White,
    background         = EcoBackground,       // Off-White background
    onBackground       = EcoTextHigh,         // Dark slate gray text
    surface            = EcoSurface,          // Pure white card
    onSurface          = EcoTextHigh,
    surfaceVariant     = Color(0xFFF8F9FA),
    onSurfaceVariant   = EcoTextMedium,       // Medium gray
    outline            = EcoDivider,          // Light gray boundary
    error              = StatusCancelled,
)

@Composable
fun SisvvappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
