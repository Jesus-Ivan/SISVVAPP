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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.sync.SyncWorker
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.AppNavigationDrawerContent
import com.example.sisvvapp.ui.navigation.NavGraphs
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.screens.ajustes.AjustesScreen
import com.example.sisvvapp.ui.screens.socios.PerfilSocioScreen
import com.example.sisvvapp.ui.screens.socios.SociosScreen
import com.example.sisvvapp.ui.screens.ventas.BuscarProductosScreen
import com.example.sisvvapp.ui.screens.ventas.DetalleVentaScreen
import com.example.sisvvapp.ui.screens.ventas.NuevaVentaConfigScreen
import com.example.sisvvapp.ui.screens.ventas.ResumenCarritoScreen
import com.example.sisvvapp.ui.screens.ventas.SeleccionarModificadoresScreen

import com.example.sisvvapp.ui.screens.ventas.VentasScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.viewmodel.CarritoViewModel
import com.example.sisvvapp.ui.viewmodel.CajaViewModel
import com.example.sisvvapp.ui.viewmodel.ModificadoresViewModel
import com.example.sisvvapp.ui.viewmodel.NuevaVentaViewModel

import com.example.sisvvapp.ui.viewmodel.SendResult
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import com.example.sisvvapp.ui.viewmodel.SociosViewModel
import com.example.sisvvapp.ui.viewmodel.VentasViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@Composable
fun MainContainer(
    viewModel: SisvvViewModel? = null,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val factory = SisvvViewModelFactory(context)
    val sharedCajaViewModel: CajaViewModel = viewModel(factory = factory)
    val db = AppDatabase.getInstance(context)
    val pendientesCount by db.ventaColaDao().countPendientesFlow().collectAsState(initial = 0)

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoutes.VENTAS

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
                viewModel = viewModel,
                pendientesCount = pendientesCount
            )
        }
    ) {
        NavHost(navController = navController, startDestination = NavGraphs.VENTAS_GRAPH) {

            // ================================================================
            // VENTAS GRAPH
            // ================================================================
            navigation(
                startDestination = ScreenRoutes.VENTAS,
                route = NavGraphs.VENTAS_GRAPH
            ) {

                // --- PANTALLA DE VENTAS ---
                composable(ScreenRoutes.VENTAS) {
                    val ventasViewModel: VentasViewModel = viewModel(factory = factory)
                    val ventas by ventasViewModel.ventas.collectAsState()
                    val isLoading by ventasViewModel.isLoading.collectAsState()
                    val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                    val cajas by sharedCajaViewModel.cajas.collectAsState()
                    val cajaActiva = cajas.find { it.id == selectedCajaId }
                    val corteCajaActivo = cajaActiva?.corte

                    // 1. Memoria local para el buscador
                    var searchQuery by remember { mutableStateOf("") }

                    // 2. Filtro en tiempo real para las ventas
                    val ventasFiltradas = if (searchQuery.isBlank()) {
                        ventas
                    } else {
                        ventas.filter { venta ->
                            venta.folio.toString().contains(searchQuery, ignoreCase = true) ||
                                    venta.nombreCliente.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    LaunchedEffect(selectedCajaId, cajas) {
                        val today = java.time.LocalDate.now().toString()
                        ventasViewModel.refreshVentas(today, corteCajaActivo)
                    }

                    VentasScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        ventas = ventasFiltradas, // Pasamos la lista filtrada
                        isOnline = viewModel?.isOnline ?: true,
                        isLoading = isLoading,

                        // 3. Conectamos los parámetros de la búsqueda
                        searchQuery = searchQuery,
                        onSearchQueryChange = { nuevoTexto ->
                            searchQuery = nuevoTexto
                        },

                        onRefresh = {
                            val today = java.time.LocalDate.now().toString()
                            ventasViewModel.refreshVentas(today, corteCajaActivo)
                        },
                        onVentaClick = { venta ->
                            navController.navigate(ScreenRoutes.crearRutaDetalleVenta(venta.folio))
                        },
                        onNuevaVentaClick = {
                            navController.navigate(ScreenRoutes.NUEVA_VENTA)
                        },
                        onDateSelected = { fecha ->
                            ventasViewModel.refreshVentas(fecha, corteCajaActivo)
                        }
                    )
                }

                // --- PANTALLA DE NUEVA VENTA (CONFIGURACIÓN) ---
                composable(ScreenRoutes.NUEVA_VENTA) { backStackEntry ->
                    val nuevaVentaViewModel: NuevaVentaViewModel = viewModel(factory = factory)

                    val saleGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavGraphs.VENTAS_GRAPH)
                    }
                    val carritoViewModel: CarritoViewModel = viewModel<CarritoViewModel>(viewModelStoreOwner = saleGraphEntry, factory = factory)

                    val tipoVenta by nuevaVentaViewModel.tipoVenta.collectAsState()
                    val searchQuery by nuevaVentaViewModel.searchQuery.collectAsState()
                    val sociosEncontrados by nuevaVentaViewModel.sociosEncontrados.collectAsState()
                    val nombreCliente by nuevaVentaViewModel.nombreCliente.collectAsState()
                    val socioSeleccionado by nuevaVentaViewModel.socioSeleccionado.collectAsState()

                    val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                    val cajas by sharedCajaViewModel.cajas.collectAsState()
                    val cajasDisponibles = cajas.isNotEmpty()
                    val cajaActiva = cajas.find { it.id == selectedCajaId }
                    val corteCaja = cajaActiva?.corte ?: 0
                    val clavePuntoVenta = cajaActiva?.clavePuntoVenta ?: ""

                    LaunchedEffect(tipoVenta, nombreCliente, corteCaja) {
                        carritoViewModel.configurarVenta(
                            tipoVenta = tipoVenta,
                            socioId = nuevaVentaViewModel.socioId.value,
                            nombreCliente = nombreCliente,
                            corteCaja = corteCaja,
                            clavePuntoVenta = clavePuntoVenta
                        )
                    }

                    val tiposVenta by nuevaVentaViewModel.tiposVenta.collectAsState()

                    NuevaVentaConfigScreen(
                        tiposDeVenta = tiposVenta,
                        tipoSeleccionado = tipoVenta,
                        onTipoVentaChange = { nuevaVentaViewModel.setTipoVenta(it) },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { nuevaVentaViewModel.searchSocios(it) },
                        sociosEncontrados = sociosEncontrados,
                        onSocioSeleccionado = { socio ->
                            if (socio != null) nuevaVentaViewModel.selectSocio(socio)
                            else nuevaVentaViewModel.clearSocioSelection()
                        },
                        socioSeleccionado = socioSeleccionado,
                        nombreCliente = nombreCliente,
                        onNombreClienteChange = { nuevaVentaViewModel.setNombreCliente(it) },
                        isOnline = viewModel?.isOnline ?: true,
                        onMenuClick = { navController.popBackStack() },
                        onContinuarClick = {
                            navController.navigate(ScreenRoutes.BUSCAR_PRODUCTOS)
                        },
                        cajasDisponibles = cajasDisponibles,
                        isFormValid = nuevaVentaViewModel.isFormValid()
                    )

                }

                // --- PANTALLA DE BUSCAR PRODUCTOS ---
                composable(ScreenRoutes.BUSCAR_PRODUCTOS) { backStackEntry ->
                    val saleGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavGraphs.VENTAS_GRAPH)
                    }
                    val carritoViewModel: CarritoViewModel = viewModel<CarritoViewModel>(viewModelStoreOwner = saleGraphEntry, factory = factory)

                    val productos by carritoViewModel.productos.collectAsState()
                    val searchQuery by carritoViewModel.searchQuery.collectAsState()
                    val items by carritoViewModel.items.collectAsState()

                    BuscarProductosScreen(
                        productos = productos,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { carritoViewModel.searchProductos(it) },
                        carritoCount = items.size,
                        onAddProducto = { producto, cantidad ->
                            carritoViewModel.addProducto(producto, cantidad)
                        },
                        onProductoConModificadores = { producto, cantidad ->
                            carritoViewModel.seleccionarProducto(producto, cantidad)
                            navController.navigate(ScreenRoutes.crearRutaModificadores(producto.id))
                        },
                        onVerCarrito = {
                            navController.navigate(ScreenRoutes.RESUMEN_CARRITO)
                        },
                        onBackClick = { navController.popBackStack() },
                        isOnline = viewModel?.isOnline ?: true
                    )
                }

                // --- PANTALLA DE MODIFICADORES ---
                composable(
                    route = ScreenRoutes.MODIFICADORES,
                    arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0

                    val saleGraphEntry = remember(navController.currentBackStackEntry) {
                        navController.getBackStackEntry(NavGraphs.VENTAS_GRAPH)
                    }
                    val carritoViewModel: CarritoViewModel = viewModel<CarritoViewModel>(viewModelStoreOwner = saleGraphEntry, factory = factory)
                    val productos by carritoViewModel.productos.collectAsState()
                    val producto = productos.find { it.id == productoId }

                    val modificadoresViewModel: ModificadoresViewModel = viewModel(factory = factory)
                    val grupos by modificadoresViewModel.grupos.collectAsState()
                    val modificadores by modificadoresViewModel.modificadores.collectAsState()

                    LaunchedEffect(productoId) {
                        modificadoresViewModel.cargarModificadores(productoId)
                    }

                    if (producto != null) {
                        SeleccionarModificadoresScreen(
                            producto = producto,
                            gruposModificadores = grupos,
                            modificadoresDisponibles = modificadores,
                            onAddToCart = { mods ->
                                carritoViewModel.addProductoConModificadores(producto, mods, grupos, carritoViewModel.cantidadSeleccionada)
                                navController.popBackStack()
                            },
                            onBackClick = { navController.popBackStack() },
                            isOnline = viewModel?.isOnline ?: true
                        )
                    }
                }

                // --- PANTALLA DE RESUMEN CARRITO ---
                composable(ScreenRoutes.RESUMEN_CARRITO) { backStackEntry ->
                    val saleGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavGraphs.VENTAS_GRAPH)
                    }
                    val carritoViewModel: CarritoViewModel = viewModel<CarritoViewModel>(viewModelStoreOwner = saleGraphEntry, factory = factory)

                    val items by carritoViewModel.items.collectAsState()
                    val total by carritoViewModel.total.collectAsState()
                    val tipoVenta by carritoViewModel.tipoVenta.collectAsState()
                    val nombreCliente by carritoViewModel.nombreCliente.collectAsState()
                    val corteCaja by carritoViewModel.corteCaja.collectAsState()
                    val isSending by carritoViewModel.isSending.collectAsState()
                    val sendResult by carritoViewModel.sendResult.collectAsState()

                    val isAppend = carritoViewModel.esModoAppend()

                    LaunchedEffect(sendResult) {
                        if (sendResult is SendResult.Success) {
                            delay(1500L)
                            carritoViewModel.clearState()
                            navController.navigate(ScreenRoutes.VENTAS) {
                                popUpTo(ScreenRoutes.VENTAS) { inclusive = true }
                            }
                        }
                    }

                    ResumenCarritoScreen(
                        items = items,
                        tipoVenta = if (isAppend) "Agregar productos" else tipoVenta,
                        nombreCliente = nombreCliente,
                        corteCaja = corteCaja,
                        total = total,
                        isSending = isSending,
                        sendResult = sendResult,
                        onUpdateCantidad = { index, cant -> carritoViewModel.updateCantidad(index, cant) },
                        onRemoveItem = { index -> carritoViewModel.removeProducto(index) },
                        onConfirmar = {
                            carritoViewModel.confirmarVenta()
                        },
                        onVolver = {
                            carritoViewModel.clearState()
                            navController.navigate(ScreenRoutes.VENTAS) {
                                popUpTo(ScreenRoutes.VENTAS) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() },
                        isOnline = viewModel?.isOnline ?: true
                    )
                }



                // --- PANTALLA DE DETALLE DE VENTA ---
                composable(
                    route = ScreenRoutes.DETALLE_VENTA,
                    arguments = listOf(navArgument("folio") { type = NavType.IntType })
                ) { backStackEntry ->
                    val folio = backStackEntry.arguments?.getInt("folio") ?: 0
                    val ventasViewModel: VentasViewModel = viewModel(factory = factory)

                    val saleGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavGraphs.VENTAS_GRAPH)
                    }
                    val carritoViewModel: CarritoViewModel = viewModel<CarritoViewModel>(
                        viewModelStoreOwner = saleGraphEntry,
                        factory = factory
                    )

                    var ventaDetalle by remember { mutableStateOf<VentaDto?>(null) }
                    var isLoadingDetalle by remember { mutableStateOf(true) }

                    LaunchedEffect(folio) {
                        isLoadingDetalle = true
                        ventaDetalle = ventasViewModel.cargarDetalle(folio)
                        isLoadingDetalle = false
                    }

                    DetalleVentaScreen(
                        venta = ventaDetalle,
                        isLoading = isLoadingDetalle,
                        isOnline = viewModel?.isOnline ?: true,
                        onBackClick = { navController.popBackStack() },
                        onAgregarProductos = {
                            carritoViewModel.configurarAppendMode(
                                folio = folio,
                                nombreCliente = ventaDetalle?.nombreCliente ?: "",
                                clavePuntoVenta = ventaDetalle?.clavePuntoVenta ?: ""
                            )
                            navController.navigate(ScreenRoutes.BUSCAR_PRODUCTOS)
                        }
                    )
                }
            }

            // ================================================================
            // SOCIOS GRAPH
            // ================================================================
            navigation(
                startDestination = ScreenRoutes.SOCIOS,
                route = NavGraphs.SOCIOS_GRAPH
            ) {

                // --- PANTALLA DE SOCIOS ---
                composable(ScreenRoutes.SOCIOS) { backStackEntry ->
                    val sociosGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavGraphs.SOCIOS_GRAPH)
                    }
                    val sociosViewModel: SociosViewModel = viewModel<SociosViewModel>(viewModelStoreOwner = sociosGraphEntry, factory = factory)

                    val socios by sociosViewModel.socios.collectAsState()
                    val isLoading by sociosViewModel.isLoading.collectAsState()
                    val errorMessage by sociosViewModel.error.collectAsState()
                    val searchQuery by sociosViewModel.searchQuery.collectAsState()

                    SociosScreen(
                        socios = socios,
                        isLoading = isLoading,
                        isOnline = viewModel?.isOnline ?: true,
                        searchQuery = searchQuery,
                        errorMessage = errorMessage,
                        onSearchQueryChange = { query ->
                            sociosViewModel.search(query)
                        },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSocioClick = { socioId ->
                            navController.navigate(ScreenRoutes.crearRutaPerfilSocio(socioId))
                        },
                        onRetry = { sociosViewModel.sync() },
                        onRefresh = {
                            sociosViewModel.search("")
                            sociosViewModel.sync()
                        }
                    )
                }

                // --- PERFIL DEL SOCIO ---
                composable(
                    route = ScreenRoutes.PERFIL_SOCIO,
                    arguments = listOf(navArgument("socioId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val socioId = backStackEntry.arguments?.getInt("socioId") ?: 0

                    val sociosGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavGraphs.SOCIOS_GRAPH)
                    }
                    val sociosViewModel: SociosViewModel = viewModel<SociosViewModel>(viewModelStoreOwner = sociosGraphEntry, factory = factory)

                    val socioSeleccionado by sociosViewModel.selectedSocio.collectAsState()
                    val integrantes by sociosViewModel.integrantes.collectAsState(initial = emptyList())

                    LaunchedEffect(socioId) {
                        sociosViewModel.getIntegrantesPorSocio(socioId)
                    }

                    if (socioSeleccionado != null) {
                        PerfilSocioScreen(
                            socio = socioSeleccionado!!,
                            integrantes = integrantes,
                            isOnline = viewModel?.isOnline ?: true,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VerdePrincipal)
                        }
                    }
                }
            }

            // ================================================================
            // AJUSTES GRAPH
            // ================================================================
            navigation(
                startDestination = ScreenRoutes.AJUSTES,
                route = NavGraphs.AJUSTES_GRAPH
            ) {

                // --- PANTALLA DE AJUSTES ---
                composable(ScreenRoutes.AJUSTES) {
                    LaunchedEffect(currentRoute) {
                        if (currentRoute == ScreenRoutes.AJUSTES) {
                            sharedCajaViewModel.refreshCajas()
                        }
                    }

                    val cajas by sharedCajaViewModel.cajas.collectAsState()
                    val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                    val isLoading by sharedCajaViewModel.isLoading.collectAsState()
                    val cajasDto = cajas.map { entity ->
                        CajaDto(entity.id, entity.nombre, entity.fechaApertura, entity.fechaCierre, entity.activo, entity.meseroId)
                    }
                    val sessionManager = SessionManager.getInstance(context)
                    val lastSyncTimestamp = sessionManager.getLastSyncDate()
                    val lastSyncText = if (lastSyncTimestamp > 0) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(lastSyncTimestamp))
                    } else {
                        "Pendiente de sincronizar"
                    }
                    AjustesScreen(
                        cajas = cajasDto,
                        selectedCajaId = selectedCajaId,
                        lastSyncDate = lastSyncText,
                        isLoading = isLoading,
                        isOnline = viewModel?.isOnline ?: true,
                        themeMode = viewModel?.themeMode ?: 0,
                        onThemeModeChange = { viewModel?.updateThemeMode(it) },
                        onCajaClick = { caja -> sharedCajaViewModel.selectCaja(caja.id, caja.nombre) },
                        onSyncClick = {
                            SyncWorker.enqueueOneTime(context)
                            android.widget.Toast.makeText(context, "Sincronización iniciada en segundo plano", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onLogoutClick = { onLogout() },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onRefresh = { scope.launch { sharedCajaViewModel.refreshCajas() } }
                    )
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