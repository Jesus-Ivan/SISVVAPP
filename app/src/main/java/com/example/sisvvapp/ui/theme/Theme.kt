package com.example.sisvvapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // Colores Principales
    primary = VerdePremiumDark,
    onPrimary = Grey950,
    primaryContainer = EstadoExitoOscuro,
    onPrimaryContainer = TextoExitoOscuro,

    // Fondos y Superficies (Jerarquía SaaS)
    background = Grey900,
    onBackground = Grey100,
    surface = Grey850,
    onSurface = Grey100,
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey400,

    // Bordes y Divisores
    outline = Grey700,
    outlineVariant = Grey800,

    // Alertas y Estados (Mapping Semántico)
    secondaryContainer = EstadoAlertaOscuro,
    onSecondaryContainer = TextoAlertaOscuro,
    
    error = RedSaaS,
    errorContainer = FondoErrorOscuro,
    onErrorContainer = TextoErrorOscuro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = FondoAbierta,
    onPrimaryContainer = TextoAbierta,

    background = FondoAppClaro,
    onBackground = TextoPrincipalClaro,
    surface = FondoCardsClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = Color(0xFFF1F1F4),
    onSurfaceVariant = TextoSecundarioClaro,

    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFF4F4F5),

    // Alertas y Estados (Mapping Semántico)
    secondaryContainer = EstadoAlertaClaro,
    onSecondaryContainer = Color(0xFF92400E), // Marrón/Naranja oscuro para contraste sobre amarillo

    error = RedSaaS,
    errorContainer = FondoErrorClaro,
    onErrorContainer = TextoErrorFuerte
)

@Composable
fun SISVVAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
