package com.example.sisvvapp

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.navigation.Screen
import com.example.sisvvapp.ui.screens.caja.CajaScreen
import com.example.sisvvapp.ui.screens.login.LoginScreen
import com.example.sisvvapp.ui.screens.main.MainContainer
import com.example.sisvvapp.ui.screens.splash.SplashScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.viewmodel.CajaViewModel
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import com.example.sisvvapp.data.sync.SyncWorker

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Programar sync periódico
        SyncWorker.enqueuePeriodic(this)

        setContent {
            val sisvvViewModel: SisvvViewModel = viewModel(
                factory = SisvvViewModelFactory(this)
            )

            val isDarkTheme = when (sisvvViewModel.themeMode) {
                1 -> false // Claro
                2 -> true  // Oscuro
                else -> isSystemInDarkTheme() // Sistema (0)
            }

            SISVVAPPTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)

                val deviceType =
                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                        DeviceType.MOBILE
                    } else {
                        DeviceType.TABLET
                    }

                CompositionLocalProvider(LocalDeviceType provides deviceType) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        // --- 1. PANTALLA DE SPLASH ---
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- 2. PANTALLA DE LOGIN ---
                        composable(Screen.Login.route) {
                            val isLoading = sisvvViewModel.isLoading
                            val loginSuccess = sisvvViewModel.loginSuccess
                            val loginError =
                                sisvvViewModel.loginError ?: sisvvViewModel.networkError

                            LaunchedEffect(loginSuccess) {
                                if (loginSuccess) {
                                    navController.navigate(Screen.CajaInicial.route) {
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

                        // --- 2.1 PANTALLA DE SELECCIÓN DE CAJA INICIAL (POST-LOGIN OBLIGATORIO) ---
                        composable(Screen.CajaInicial.route) {
                            val cajaViewModel: CajaViewModel = viewModel(factory = SisvvViewModelFactory(this@MainActivity))

                            val cajas by cajaViewModel.cajas.collectAsState()
                            val selectedCajaId by cajaViewModel.selectedCajaId.collectAsState()
                            val isLoading by cajaViewModel.isLoading.collectAsState()

                            val cajasDto = cajas.map { entity ->
                                CajaDto(entity.id, entity.nombre, entity.fechaApertura, entity.fechaCierre, entity.activo, entity.meseroId)
                            }

                            CajaScreen(
                                cajas = cajasDto,
                                selectedCajaId = selectedCajaId,
                                isLoading = isLoading,
                                isOnline = sisvvViewModel.isOnline,
                                isFromSettings = false,
                                onCajaClick = { id -> cajaViewModel.selectCaja(id) },
                                onNavigationClick = {},
                                onContinueClick = { id ->
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.CajaInicial.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- 3. CONTENEDOR PRINCIPAL (Ventas, Socios, Ajustes) ---
                        composable(Screen.Main.route) {
                            MainContainer(
                                viewModel = sisvvViewModel,
                                windowWidthSizeClass = windowSizeClass.widthSizeClass,
                                onLogout = {
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
}