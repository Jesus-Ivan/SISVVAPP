package com.example.sisvvapp.ui.navigation

// ─── Navigation Destinations ──────────────────────────────────────────────────
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login  : Screen("login")
    object Main   : Screen("main")
}
