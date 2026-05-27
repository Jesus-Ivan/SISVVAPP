package com.example.sisvvapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // Colores Principales
    primary = VerdePremiumDark,
    onPrimary = Grey950,
    primaryContainer = VerdePremiumSoft,
    onPrimaryContainer = VerdePremiumDark,

    // Fondos y Superficies (Jerarquía SaaS)
    background = Grey900,
    onBackground = Grey100,
    surface = Grey850,         // Las tarjetas heredarán este color automáticamente
    onSurface = Grey100,
    surfaceVariant = Grey800,  // Inputs y elementos decorativos
    onSurfaceVariant = Grey400,

    // Bordes y Divisores
    outline = Grey700,
    outlineVariant = Grey800,

    // Estados
    error = RedSaaS,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = RedSaaS,
    
    // Mapeo para componentes personalizados (ej. Banners)
    secondaryContainer = EstadoAlertaOscuro,
    onSecondaryContainer = TextoAlertaOscuro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = EstadoExitoClaro,
    onPrimaryContainer = VerdePrincipal,

    background = FondoAppClaro,
    onBackground = TextoPrincipalClaro,
    surface = FondoCardsClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = Color(0xFFF1F1F4),
    onSurfaceVariant = TextoSecundarioClaro,

    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFF4F4F5),

    error = RedSaaS,
    errorContainer = EstadoAlertaClaro,
    onErrorContainer = Color(0xFF937410)
)

@Composable
fun SISVVAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val scale = rememberScaleFactor()
    val scaledTypography = scaleTypography(scale)

    CompositionLocalProvider(LocalScaleFactor provides scale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}
