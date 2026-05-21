package com.example.sisvvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sisvvapp.ui.navigation.Screen
import com.example.sisvvapp.ui.screens.login.LoginScreen
import com.example.sisvvapp.ui.screens.main.MainContainer
import com.example.sisvvapp.ui.screens.splash.SplashScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SISVVAPPTheme(darkTheme = false) {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)
                // Shared ViewModel across the whole main graph
                val sisvvViewModel: SisvvViewModel = viewModel()

                NavHost(
                    navController    = navController,
                    startDestination = Screen.Splash.route
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
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
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
