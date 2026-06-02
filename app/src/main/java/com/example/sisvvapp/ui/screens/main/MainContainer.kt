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
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.AppNavigationDrawerContent
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.screens.ajustes.AjustesScreen
import com.example.sisvvapp.ui.screens.socios.PerfilSocioScreen
import com.example.sisvvapp.ui.screens.socios.SociosScreen
import com.example.sisvvapp.ui.screens.ventas.BuscarProductosScreen
import com.example.sisvvapp.ui.screens.ventas.NuevaVentaConfigScreen
import com.example.sisvvapp.ui.screens.ventas.ResumenCarritoScreen
import com.example.sisvvapp.ui.screens.ventas.SeleccionarModificadoresScreen
import com.example.sisvvapp.ui.screens.ventas.SeleccionarPagoScreen
import com.example.sisvvapp.ui.screens.ventas.VentasScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.viewmodel.CarritoViewModel
import com.example.sisvvapp.ui.viewmodel.CajaViewModel
import com.example.sisvvapp.ui.viewmodel.NuevaVentaViewModel
import com.example.sisvvapp.ui.viewmodel.PagoViewModel
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
                viewModel = viewModel
            )
        }
    ) {
        NavHost(navController = navController, startDestination = ScreenRoutes.VENTAS) {

            // --- PANTALLA DE VENTAS ---
            composable(ScreenRoutes.VENTAS) {
                val context = LocalContext.current
                val ventasViewModel: VentasViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val ventas by ventasViewModel.ventas.collectAsState()
                val isLoading by ventasViewModel.isLoading.collectAsState()

                LaunchedEffect(Unit) {
                    val today = java.time.LocalDate.now().toString()
                    ventasViewModel.loadVentas(today)
                }
                VentasScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    ventas = ventas,
                    isOnline = viewModel?.isOnline ?: true,
                    isLoading = isLoading,
                    onRefresh = {
                        val today = java.time.LocalDate.now().toString()
                        ventasViewModel.loadVentas(today)
                    },
                    onVentaClick = { _ -> },
                    onNuevaVentaClick = {
                        navController.navigate(ScreenRoutes.NUEVA_VENTA)
                    }
                )
            }

            // --- PANTALLA DE NUEVA VENTA (CONFIGURACIÓN) ---
            composable(ScreenRoutes.NUEVA_VENTA) {
                val context = LocalContext.current
                val nuevaVentaViewModel: NuevaVentaViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val carritoViewModel: CarritoViewModel = viewModel(factory = SisvvViewModelFactory(context))

                val tipoVenta by nuevaVentaViewModel.tipoVenta.collectAsState()
                val searchQuery by nuevaVentaViewModel.searchQuery.collectAsState()
                val sociosEncontrados by nuevaVentaViewModel.sociosEncontrados.collectAsState()
                val nombreCliente by nuevaVentaViewModel.nombreCliente.collectAsState()

                // Configurar carrito con los datos de esta venta
                val sessionManager = SessionManager.getInstance(context)
                val cajas by viewModel<CajaViewModel>(factory = SisvvViewModelFactory(context)).cajas.collectAsState()
                val corteCaja = cajas.firstOrNull()?.corte ?: 0
                val clavePuntoVenta = cajas.firstOrNull()?.clavePuntoVenta ?: ""

                LaunchedEffect(tipoVenta, nombreCliente, corteCaja) {
                    carritoViewModel.configurarVenta(
                        tipoVenta = tipoVenta,
                        socioId = nuevaVentaViewModel.socioId.value,
                        nombreCliente = nombreCliente,
                        corteCaja = corteCaja,
                        clavePuntoVenta = clavePuntoVenta
                    )
                }

                NuevaVentaConfigScreen(
                    tiposDeVenta = listOf(
                        "Público General",
                        "Socio",
                        "Invitado del Socio",
                        "Empleado"
                    ),
                    tipoSeleccionado = tipoVenta,
                    onTipoVentaChange = { nuevaVentaViewModel.setTipoVenta(it) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { nuevaVentaViewModel.searchSocios(it) },
                    sociosEncontrados = sociosEncontrados,
                    onSocioSeleccionado = { nuevaVentaViewModel.selectSocio(it) },
                    nombreCliente = nombreCliente,
                    onNombreClienteChange = { nuevaVentaViewModel.setNombreCliente(it) },
                    isOnline = viewModel?.isOnline ?: true,
                    onMenuClick = { navController.popBackStack() },
                    onContinuarClick = {
                        navController.navigate(ScreenRoutes.BUSCAR_PRODUCTOS)
                    }
                )
            }

            // --- PANTALLA DE BUSCAR PRODUCTOS ---
            composable(ScreenRoutes.BUSCAR_PRODUCTOS) {
                val context = LocalContext.current
                val carritoViewModel: CarritoViewModel = viewModel(factory = SisvvViewModelFactory(context))
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
                    onProductoClick = { producto ->
                        carritoViewModel.seleccionarProducto(producto)
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
                val context = LocalContext.current
                val carritoViewModel: CarritoViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val productos by carritoViewModel.productos.collectAsState()
                val producto = productos.find { it.id == productoId }

                if (producto != null) {
                    val db = com.example.sisvvapp.data.local.AppDatabase.getInstance(context)
                    val api = com.example.sisvvapp.network.RetrofitClient.create(context)
                    val productoRepository = com.example.sisvvapp.data.repository.ProductoRepository(
                        api, db.productoDao(), db.grupoModificadorDao()
                    )
                    var grupos by remember { mutableStateOf(emptyList<com.example.sisvvapp.data.local.entity.GrupoModificadorEntity>()) }
                    var modificadores by remember { mutableStateOf(emptyList<com.example.sisvvapp.data.local.entity.ModificadorEntity>()) }

                    LaunchedEffect(productoId) {
                        grupos = productoRepository.getGruposPorProducto(productoId)
                        val result = productoRepository.getProductoConModificadores(productoId)
                        modificadores = result?.modificadores ?: emptyList()
                    }

                    SeleccionarModificadoresScreen(
                        producto = producto,
                        gruposModificadores = grupos,
                        modificadoresDisponibles = modificadores,
                        onAddToCart = { mods ->
                            carritoViewModel.addProductoConModificadores(producto, mods)
                            navController.popBackStack()
                        },
                        onBackClick = { navController.popBackStack() },
                        isOnline = viewModel?.isOnline ?: true
                    )
                }
            }

            // --- PANTALLA DE RESUMEN CARRITO ---
            composable(ScreenRoutes.RESUMEN_CARRITO) {
                val context = LocalContext.current
                val carritoViewModel: CarritoViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val items by carritoViewModel.items.collectAsState()
                val total by carritoViewModel.total.collectAsState()
                val tipoVenta by carritoViewModel.tipoVenta.collectAsState()
                val nombreCliente by carritoViewModel.nombreCliente.collectAsState()
                val corteCaja by carritoViewModel.corteCaja.collectAsState()
                val isSending by carritoViewModel.isSending.collectAsState()
                val sendResult by carritoViewModel.sendResult.collectAsState()

                ResumenCarritoScreen(
                    items = items,
                    tipoVenta = tipoVenta,
                    nombreCliente = nombreCliente,
                    corteCaja = corteCaja,
                    total = total,
                    isSending = isSending,
                    sendResult = sendResult,
                    onConfirmar = {
                        navController.navigate(ScreenRoutes.SELECCIONAR_PAGO)
                    },
                    onVolver = {
                        carritoViewModel.limpiarCarrito()
                        navController.navigate(ScreenRoutes.VENTAS) {
                            popUpTo(ScreenRoutes.VENTAS) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                    isOnline = viewModel?.isOnline ?: true
                )
            }

            // --- PANTALLA DE SELECCIONAR PAGO ---
            composable(ScreenRoutes.SELECCIONAR_PAGO) {
                val context = LocalContext.current
                val carritoViewModel: CarritoViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val pagoViewModel: PagoViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val total by carritoViewModel.total.collectAsState()
                val tiposPago by pagoViewModel.tiposPago.collectAsState()
                val pagos by pagoViewModel.pagos.collectAsState()
                val totalPagos by pagoViewModel.montoTotal.collectAsState()
                val isSending by carritoViewModel.isSending.collectAsState()
                val sendResult by carritoViewModel.sendResult.collectAsState()

                SeleccionarPagoScreen(
                    tiposPago = tiposPago,
                    pagos = pagos,
                    totalVenta = total,
                    totalPagos = totalPagos,
                    onAgregarPago = { tipo, monto, propina ->
                        pagoViewModel.agregarPago(tipo, monto, propina)
                    },
                    onEliminarPago = { index ->
                        pagoViewModel.eliminarPago(index)
                    },
                    onConfirmar = {
                        carritoViewModel.setPagos(pagoViewModel.toPagoRequests())
                        carritoViewModel.confirmarVenta()
                    },
                    onBackClick = { navController.popBackStack() },
                    isOnline = viewModel?.isOnline ?: true
                )
            }

            // --- PANTALLA DE SOCIOS ---
            composable(ScreenRoutes.SOCIOS) {
                val context = LocalContext.current
                val sociosViewModel: SociosViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val socios by sociosViewModel.socios.collectAsState()
                val isLoading by sociosViewModel.isLoading.collectAsState()
                val errorMessage by sociosViewModel.error.collectAsState()
                var searchQuery by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    sociosViewModel.search(searchQuery)
                }

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
                    onRetry = {
                        searchQuery = ""
                        sociosViewModel.search("")
                        sociosViewModel.sync() },
                    onRefresh = { searchQuery = ""
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
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdePrincipal)
                    }
                }
            }

            // --- PANTALLA DE AJUSTES ---
            composable(ScreenRoutes.AJUSTES) {
                val context = LocalContext.current
                val cajaViewModel: CajaViewModel = viewModel(factory = SisvvViewModelFactory(context))
                val cajas by cajaViewModel.cajas.collectAsState()
                val selectedCajaId by cajaViewModel.selectedCajaId.collectAsState()
                val isLoading by cajaViewModel.isLoading.collectAsState()
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
                    onCajaClick = { caja -> cajaViewModel.selectCaja(caja.id) },
                    onSyncClick = { cajaViewModel.sync() },
                    onLogoutClick = { onLogout() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onRefresh = { cajaViewModel.sync() }
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
