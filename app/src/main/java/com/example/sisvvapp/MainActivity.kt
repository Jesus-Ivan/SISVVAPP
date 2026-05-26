package com.example.sisvvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge // Eliminado para respetar la barra de estado
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.ui.navigation.Screen
import com.example.sisvvapp.ui.screens.login.LoginScreen
import com.example.sisvvapp.ui.screens.main.MainContainer
import com.example.sisvvapp.ui.screens.splash.SplashScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Se comenta para respetar la barra de estado del sistema (batería, hora, etc.)

        val sessionManager = SessionManager.getInstance(this)

        setContent {
            SISVVAPPTheme(darkTheme = false) {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)
                val sisvvViewModel: SisvvViewModel = viewModel(
                    factory = SisvvViewModelFactory(this)
                )

                NavHost(
                    navController    = navController,
                    startDestination = Screen.Splash.route
                ) {
                    // --- 1. PANTALLA DE SPLASH ---
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigateToLogin = {
                                val destination = if (sessionManager.isLoggedIn()) {
                                    Screen.Main.route
                                } else {
                                    Screen.Login.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // --- 2. PANTALLA DE LOGIN ---
                    composable(Screen.Login.route) {
                        val isLoading = sisvvViewModel.isLoading
                        val loginSuccess = sisvvViewModel.loginSuccess
                        val loginError = sisvvViewModel.loginError ?: sisvvViewModel.networkError

                        LaunchedEffect(loginSuccess) {
                            if (loginSuccess) {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }

                        LoginScreen(
                            isLoading = isLoading,
                            serverError = loginError,
                            onLoginClick = { email, password ->
                                sisvvViewModel.login(email, password)
                            }
                        )
                    }

                    // --- 3. CONTENEDOR PRINCIPAL (Caja, Ventas, Socios) ---
                    composable(Screen.Main.route) {
                        MainContainer(
                            viewModel            = sisvvViewModel,
                            windowWidthSizeClass = windowSizeClass.widthSizeClass,
                            onLogout             = {
                                sisvvViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
