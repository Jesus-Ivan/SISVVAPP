package com.example.sisvvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sisvvapp.ui.navigation.Screen
import com.example.sisvvapp.ui.screens.login.LoginScreen
import com.example.sisvvapp.ui.screens.splash.SplashScreen
import com.example.sisvvapp.ui.theme.SisvvappTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SisvvappTheme(darkTheme = false) {
                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = Screen.Splash.route
                ) {
                    // ── Splash ──────────────────────────────────────────────
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // ── Login ───────────────────────────────────────────────
                    // onLoginSuccess is a no-op for now; remaining screens
                    // will be wired up in a future iteration.
                    composable(Screen.Login.route) {
                        LoginScreen(onLoginSuccess = {})
                    }
                }
            }
        }
    }
}
