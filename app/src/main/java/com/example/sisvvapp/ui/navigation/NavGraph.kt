package com.example.sisvvapp.ui.navigation

// ─── Navigation Destinations ──────────────────────────────────────────────────
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login  : Screen("login")
    object CajaInicial : Screen("caja_inicial")
    object Main   : Screen("main")

    object Socios : Screen("socios")

    object DetalleSocio : Screen("detalle_socio/{socioId}") {
        fun createRoute(socioId: Int) = "detalle_socio/$socioId"
    }
}
