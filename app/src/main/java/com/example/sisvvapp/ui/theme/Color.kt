package com.example.sisvvapp.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Verde Eco-Minimal Palette (Spotify Style) ──────────────────────────────
val EcoGreenPrimary    = Color(0xFF00A86B)   // Verde Jade (Acción / CTA)
val EcoGreenSecondary  = Color(0xFF008F5A)   // Verde Secundario / Hover
val EcoBackground      = Color(0xFFFAFAFA)   // Blanco Off-White (Fondo Principal)
val EcoSurface         = Color(0xFFFFFFFF)   // Blanco Puro (Tarjetas / Contenedores)
val EcoDivider         = Color(0xFFE9ECEF)   // Gris Ultra Claro (Líneas de división)
val EcoTextHigh        = Color(0xFF1A1C1E)   // Gris Pizarra Oscuro (Texto Principal)
val EcoTextMedium      = Color(0xFF6C757D)   // Gris Medio (Texto Secundario)

// ─── Status Colors ───────────────────────────────────────────────────────────
val StatusActive       = Color(0xFF00A86B)   // Jade Green
val StatusInactive     = Color(0xFF6C757D)   // Medium Gray
val StatusCancelled    = Color(0xFFDC3545)   // Red error
val StatusOpen         = Color(0xFFFD7E14)   // Orange/Amber open
val StatusOffline      = Color(0xFFDC3545)   // Red offline banner

// ─── Semantic Mapping to Retain Compatibility and Refine Layouts ─────────────
val VVGreenDeep       = Color(0xFF0F1E19)   // Ultra-deep dark pine gray for headers
val VVGreenForest     = EcoGreenPrimary     // Eco Green Primary
val VVGreenLight      = EcoGreenSecondary   // Eco Green Hover
val VVGreenAccent     = EcoGreenPrimary     // Active Green

val VVGoldChampagne   = EcoGreenPrimary     // Spotify Green replaces gold accent!
val VVGoldLight       = Color(0xFFE6F7F0)   // Very soft pastel jade background
val VVGoldDark        = EcoGreenSecondary   // Darker jade green for active elements

// Backgrounds (Mapped to Off-white and White to shift whole app to Light Eco-Minimal)
val VVBackgroundDark  = EcoBackground       // Off-White background
val VVSurfaceDark     = EcoSurface           // White card surface
val VVSurface2Dark    = EcoSurface           // White card surface
val VVSurface3Dark    = EcoDivider           // Divider thin line

// Light equivalents
val VVBackgroundLight = EcoBackground
val VVSurfaceLight    = EcoSurface
val VVSurface2Light   = EcoDivider

// Texts (All high/medium contrast slate/gray)
val OnDark            = EcoTextHigh
val OnDarkSecondary   = EcoTextMedium
val OnLight           = EcoTextHigh
val OnLightSecondary  = EcoTextMedium
