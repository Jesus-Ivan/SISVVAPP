package com.example.sisvvapp.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.stringResource
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.AppNavigationDrawerContent
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.screens.caja.CajaScreen
import com.example.sisvvapp.ui.screens.login.LoginScreen
import com.example.sisvvapp.ui.screens.socios.PerfilSocioScreen
import com.example.sisvvapp.ui.screens.splash.SplashScreen
import com.example.sisvvapp.ui.screens.ventas.VentasScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.viewmodel.CajaViewModel
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import com.example.sisvvapp.ui.viewmodel.SociosViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@Composable
fun MainContainer(
    viewModel: SisvvViewModel? = null,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoutes.SPLASH

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != ScreenRoutes.LOGIN && currentRoute != ScreenRoutes.SPLASH,
        drawerContent = {
            AppNavigationDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = ScreenRoutes.SPLASH) {

            /* --- 1. PANTALLA DE SPLASH
            composable(ScreenRoutes.SPLASH) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(ScreenRoutes.LOGIN) {
                            popUpTo(ScreenRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            } */

            // --- 2. PANTALLA DE LOGIN ---
            composable(ScreenRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(ScreenRoutes.CAJA) {
                            popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // --- 3. PANTALLA DE CAJA  ---
            composable(ScreenRoutes.CAJA) {
                val context = LocalContext.current
                val cajaViewModel: CajaViewModel = viewModel(factory = SisvvViewModelFactory(context))

                val cajas by cajaViewModel.cajas.collectAsState()
                val selectedCajaId by cajaViewModel.selectedCajaId.collectAsState()
                val isLoading by cajaViewModel.isLoading.collectAsState()

                // Transformamos Entity a DTO para la UI
                val cajasDto = cajas.map { entity ->
                    CajaDto(entity.id, entity.nombre, entity.fechaApertura, entity.fechaCierre, entity.activo, entity.meseroId)
                }

                CajaScreen(
                    cajas = cajasDto,
                    selectedCajaId = selectedCajaId,
                    isLoading = isLoading,
                    onCajaClick = { id -> cajaViewModel.selectCaja(id) },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onContinueClick = { cajaId ->
                        navController.navigate(ScreenRoutes.VENTAS) {
                        }
                    }
                )
            }

            // --- 4. PANTALLA DE VENTAS  ---
            composable(ScreenRoutes.VENTAS) {

                VentasScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    ventas = emptyList(),
                    onVentaClick = { venta ->
                        /* Abrir detalle de la venta */
                    },
                    onNuevaVentaClick = {
                    }
                )
            }

            // --- 5. PERFIL DEL SOCIO ---
            composable(
                route = ScreenRoutes.PERFIL_SOCIO,
                arguments = listOf(navArgument("socioId") { type = NavType.IntType })
            ) { backStackEntry ->
                val socioId = backStackEntry.arguments?.getInt("socioId") ?: 0

                val context = LocalContext.current
                val sociosViewModel: SociosViewModel = viewModel(factory = SisvvViewModelFactory(context))


                val socios by sociosViewModel.socios.collectAsState()
                val socioSeleccionado = socios.find { it.id == socioId }


                val integrantes by sociosViewModel.integrantes.collectAsState(initial = emptyList())

                LaunchedEffect(socioId) {
                    sociosViewModel.getIntegrantesPorSocio(socioId)
                }

                if (socioSeleccionado != null) {
                    PerfilSocioScreen(
                        socio = socioSeleccionado,
                        integrantes = integrantes,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdePrincipal)
                    }
                }
            }

            // --- 6. PANTALLA DE AJUSTES ---
            composable(ScreenRoutes.AJUSTES) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.settings_construction))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerPreview() {
    SISVVAPPTheme {
        MainContainer()
    }
}