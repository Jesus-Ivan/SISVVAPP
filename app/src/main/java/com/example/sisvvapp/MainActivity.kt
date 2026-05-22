package com.example.sisvvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.LocalContext
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
        enableEdgeToEdge()
        setContent {
            SISVVAPPTheme(darkTheme = false) {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)
                val sisvvViewModel: SisvvViewModel = viewModel(
                    factory = SisvvViewModelFactory(this)
                )
                val startDestination = Screen.Splash.route

                NavHost(
                    navController    = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Splash.route) {
                        val sessionManager = SessionManager.getInstance(LocalContext.current)
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
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Main.route) {
                        MainContainer(
                            viewModel            = sisvvViewModel,
                            windowWidthSizeClass = windowSizeClass.widthSizeClass,
                            onLogout             = {
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
