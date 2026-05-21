package com.example.sisvvapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. ESQUEMA OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = VerdePrincipalOscuro,
    onPrimary = FondoAppOscuro,
    background = FondoAppOscuro,
    surface = FondoCardsOscuro,
    surfaceVariant = FondoInputOscuro,
    onSurface = TextoPrincipalOscuro,
    onSurfaceVariant = TextoSecundarioOscuro,

    // Usamos estos roles para los banners de conexión dinámicos
    primaryContainer = EstadoExitoOscuro,
    onPrimaryContainer = TextoExitoOscuro,
    errorContainer = EstadoAlertaOscuro,
    onErrorContainer = TextoAlertaOscuro
)

// 2. ESQUEMA CLARO (Tus códigos de Figma)
private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = FondoCardsClaro,
    background = FondoAppClaro,
    surface = FondoCardsClaro,
    surfaceVariant = FondoInputClaro,
    onSurface = TextoPrincipalClaro,
    onSurfaceVariant = TextoSecundarioClaro,

    primaryContainer = EstadoExitoClaro,
    onPrimaryContainer = VerdePrincipal,
    errorContainer = EstadoAlertaClaro,
    onErrorContainer = Color(0xFF937410)
)

@Composable
fun SISVVAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta el modo del sistema automáticamente
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // La tipografía local con Poppins e Inter que creamos
        content = content
    )
}