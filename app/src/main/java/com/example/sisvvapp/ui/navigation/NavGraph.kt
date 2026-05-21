package com.example.sisvvapp.ui.navigation

// ─── Navigation Destinations ──────────────────────────────────────────────────
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login  : Screen("login")
    // Future screens will be added here as they are implemented
}
