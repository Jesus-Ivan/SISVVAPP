package com.example.sisvvapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── PALETA DE GRISES PREMIUM (SaaS Estilo) ──────────────────────────
val Grey950 = Color(0xFF0C0C0E) // Fondo más profundo
val Grey900 = Color(0xFF121214) // Fondo base
val Grey850 = Color(0xFF1E1E22) // Superficie de Tarjetas (Elevación 1)
val Grey800 = Color(0xFF2A2A2F) // Superficie de Inputs / Hover
val Grey700 = Color(0xFF3F3F46) // Bordes sutiles y Divisores
val Grey400 = Color(0xFFA1A1AA) // Texto secundario / Deshabilitado
val Grey100 = Color(0xFFF4F4F5) // Texto Principal en Modo Oscuro

// ── VERDE BRANDING REFINADO ──────────────────────────────────────────
val VerdePrincipal = Color(0xFF226038)      // Verde Original para Modo Claro
val VerdePremiumDark = Color(0xFF54D683)    // Verde Esmeralda desaturado (No vibra, brilla)
val VerdePremiumSoft = Color(0xFF1A3826)    // Contenedor de éxito oscuro (Fondo)

// ── MODO CLARO (EXISTENTE REFINADO) ──────────────────────────────────
val FondoAppClaro = Color(0xFFF8F9FA)
val FondoCardsClaro = Color(0xFFFFFFFF)
val TextoPrincipalClaro = Color(0xFF18181B)
val TextoSecundarioClaro = Color(0xFF71717A)

// ── ALERTAS Y ESTADOS (SaaS Palette) ─────────────────────────────────
val RedSaaS = Color(0xFFEF4444)
val OrangeSaaS = Color(0xFFF59E0B)

// Colores adicionales para compatibilidad con componentes existentes
val VerdePrincipalOscuro = VerdePremiumDark
val FondoAppOscuro = Grey900
val FondoCardsOscuro = Grey850
val FondoInputOscuro = Grey800
val TextoPrincipalOscuro = Grey100
val TextoSecundarioOscuro = Grey400
val EstadoExitoOscuro = VerdePremiumSoft
val TextoExitoOscuro = VerdePremiumDark
val EstadoAlertaOscuro = Color(0xFF452E00) // Naranja muy profundo para fondo
val TextoAlertaOscuro = OrangeSaaS
val EstadoExitoClaro = Color(0xFFDCFCE7)
val EstadoAlertaClaro = Color(0xFFFEF3C7)

// Colores de estatus inactivo
val FondoInactivo = Color(0xFFF1F1F4)
val TextoInactivo = Color(0xFF71717A)
val FondoInputClaro = Color(0xFFF1F1F4)

// Otros colores específicos si se usan
val FondoAbierta = Color(0xFFE8F5E9)
val TextoAbierta = Color(0xFF2E7D32)
val FondoErrorClaro = Color(0xFFFFEBEE)
val TextoErrorFuerte = Color(0xFFC62828)
val SplashGreenDeep = Color(0xFF1B4332)
val SplashGreenMid = Color(0xFF2D6A4F)
val SplashGreenLime = Color(0xFF95D5B2)
val SplashBallGrayLight = Color(0xFFF8F9FA)
val SplashBallGrayDark = Color(0xFFDEE2E6)
