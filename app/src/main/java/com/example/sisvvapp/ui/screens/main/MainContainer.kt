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
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.AppNavigationDrawerContent
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.screens.caja.CajaScreen
import com.example.sisvvapp.ui.screens.socios.PerfilSocioScreen
import com.example.sisvvapp.ui.screens.socios.SociosScreen
import com.example.sisvvapp.ui.screens.ventas.VentasScreen
import com.example.sisvvapp.ui.screens.ajustes.AjustesScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.viewmodel.CajaViewModel
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import com.example.sisvvapp.ui.viewmodel.SociosViewModel
import com.example.sisvvapp.ui.viewmodel.VentasViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
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
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoutes.CAJA

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
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
                onCloseDrawer = { scope.launch { drawerState.close() } },
                viewModel = viewModel
            )
        }
    ) {
        NavHost(navController = navController, startDestination = ScreenRoutes.CAJA) {

            // --- 3. PANTALLA DE CAJA  ---
            composable(ScreenRoutes.CAJA) {
                val context = LocalContext.current
                val cajaViewModel: CajaViewModel = viewModel(factory = SisvvViewModelFactory(context))

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
                    isOnline = viewModel?.isOnline ?: true,
                    onCajaClick = { id -> cajaViewModel.selectCaja(id) },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onContinueClick = { _ ->
                        navController.navigate(ScreenRoutes.VENTAS) {
                        }
                    }
                )
            }

            // --- 4. PANTALLA DE VENTAS  ---
            composable(ScreenRoutes.VENTAS) {
                val context = LocalContext.current
                val ventasViewModel: VentasViewModel = viewModel(factory = SisvvViewModelFactory(context))

                val ventas by ventasViewModel.ventas.collectAsState()
                val isLoading by ventasViewModel.isLoading.collectAsState()
                val error by ventasViewModel.error.collectAsState()

                LaunchedEffect(Unit) {
                    val today = java.time.LocalDate.now().toString()
                    ventasViewModel.loadVentas(today)
                }

                VentasScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    ventas = ventas,
                    isOnline = viewModel?.isOnline ?: true,
                    onVentaClick = { _ ->
                    },
                    onNuevaVentaClick = {
                    }
                )
            }

            // --- 5. PANTALLA DE SOCIOS (LISTA PRINCIPAL) ---
            composable(ScreenRoutes.SOCIOS) {
                val context = LocalContext.current
                val sociosViewModel: SociosViewModel = viewModel(factory = SisvvViewModelFactory(context))

                val socios by sociosViewModel.socios.collectAsState()
                val isLoading by sociosViewModel.isLoading.collectAsState()
                val errorMessage by sociosViewModel.error.collectAsState()

                var searchQuery by remember { mutableStateOf("") }

                SociosScreen(
                    socios = socios,
                    isLoading = isLoading,
                    isOnline = viewModel?.isOnline ?: true,
                    searchQuery = searchQuery,
                    errorMessage = errorMessage,
                    onSearchQueryChange = { query ->
                        searchQuery = query
                        sociosViewModel.search(query)
                    },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSocioClick = { socioId ->
                        navController.navigate(ScreenRoutes.crearRutaPerfilSocio(socioId))
                    },
                    onRetry = { sociosViewModel.sync() }
                )
            }

            // --- 5.1 PERFIL DEL SOCIO (DETALLE) ---
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
                        isOnline = viewModel?.isOnline ?: true,
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
                val context = LocalContext.current
                val cajaViewModel: CajaViewModel = viewModel(factory = SisvvViewModelFactory(context))

                val cajas by cajaViewModel.cajas.collectAsState()
                val selectedCajaId by cajaViewModel.selectedCajaId.collectAsState()

                val cajasDto = cajas.map { entity ->
                    CajaDto(entity.id, entity.nombre, entity.fechaApertura, entity.fechaCierre, entity.activo, entity.meseroId)
                }

                AjustesScreen(
                    cajas = cajasDto,
                    selectedCajaId = selectedCajaId,
                    lastSyncDate = "Pendiente de sincronizar",
                    isOnline = viewModel?.isOnline ?: true,
                    onCajaClick = { caja -> cajaViewModel.selectCaja(caja.id) },
                    onSyncClick = { /* Lógica de Retrofit pendiente */ },
                    onLogoutClick = { onLogout() },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
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