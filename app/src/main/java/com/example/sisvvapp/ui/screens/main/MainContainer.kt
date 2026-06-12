package com.example.sisvvapp.ui.screens.main

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.sisvvapp.data.monitor.NetworkMonitor
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.data.sync.SyncWorker
import com.example.sisvvapp.network.RetrofitClient
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.ventas.ProductoVentaDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.AppNavigationDrawerContent
import com.example.sisvvapp.ui.components.TransferirProductoDialog
import com.example.sisvvapp.ui.navigation.NavGraphs
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.screens.ajustes.AjustesScreen
import com.example.sisvvapp.ui.screens.socios.PerfilSocioScreen
import com.example.sisvvapp.ui.screens.socios.SociosScreen
import com.example.sisvvapp.ui.screens.ventas.*
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.viewmodel.*
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

    val snackbarHostState = remember { SnackbarHostState() }
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkMonitor = remember { NetworkMonitor(connectivityManager) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    
    val ventaRepository = remember { VentaRepository(RetrofitClient.create(context), db) }
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ViewModel persistente para el flujo de ventas
    val globalCarritoViewModel: CarritoViewModel = viewModel(factory = factory)

    // Sincronización automática de la cola al recuperar conexión
    LaunchedEffect(isConnected) {
        if (isConnected) {
            delay(1000) // Estabilidad
            scope.launch {
                val procesadas = ventaRepository.procesarColaVentas()
                if (procesadas > 0) {
                    snackbarHostState.showSnackbar(
                        message = "Se sincronizaron $procesadas ventas pendientes.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoutes.VENTAS

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        ModalNavigationDrawer(
            modifier = Modifier.padding(paddingValues),
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
                navigation(startDestination = ScreenRoutes.VENTAS, route = NavGraphs.VENTAS_GRAPH) {
                    composable(ScreenRoutes.VENTAS) { backStackEntry ->
                        val ventasViewModel: VentasViewModel = viewModel(factory = factory)
                        val uiState by ventasViewModel.uiState.collectAsState()
                        val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                        val cajas by sharedCajaViewModel.cajas.collectAsState()
                        val cajaActiva = cajas.find { it.id == selectedCajaId }
                        val nombreCajaActiva = cajaActiva?.nombre ?: "Sin Caja"
                        val corteCajaActivo = cajaActiva?.corte
                        var searchQuery by remember { mutableStateOf("") }
                        var fechaActiva by remember { mutableStateOf("") }

                        LaunchedEffect(selectedCajaId, cajas, isConnected) {
                            val today = java.time.LocalDate.now().toString()
                            fechaActiva = today
                            
                            if (isConnected) {
                                // Esperamos a que la red se estabilice antes de intentar conectar a la API
                                delay(1000)
                                ventasViewModel.refreshVentas(today, corteCajaActivo)
                            } else {
                                // Si perdemos internet, forzamos el refresco para que el VM cambie a modo local/offline
                                ventasViewModel.refreshVentas(today, corteCajaActivo)
                            }
                        }

                        VentasScreen(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            uiState = uiState,
                            isOnline = isConnected,
                            selectedDate = fechaActiva,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onRefresh = { 
                                // Al refrescar manualmente, forzamos que el repositorio actualice Room
                                ventasViewModel.refreshVentas(fechaActiva, corteCajaActivo) 
                            },
                            nombreCaja = nombreCajaActiva,
                            onVentaClick = { venta ->
                                val id = if (venta.syncStatus == "RECIBIDA") venta.folio.toString() else (venta.idTemporal ?: "0")
                                navController.navigate(ScreenRoutes.crearRutaDetalleVenta(id))
                            },
                            onNuevaVentaClick = {
                                // Solo limpiamos si el carrito está vacío o ya se envió la venta anterior
                                // Esto permite reanudar si el usuario se salió por error
                                if (globalCarritoViewModel.items.value.isEmpty() && !globalCarritoViewModel.esModoAppend()) {
                                    globalCarritoViewModel.clearState()
                                }
                                navController.navigate(ScreenRoutes.NUEVA_VENTA)
                            },
                            onDateSelected = { fechaActiva = it; searchQuery = ""; ventasViewModel.refreshVentas(it, corteCajaActivo) },
                            onClearDate = { fechaActiva = java.time.LocalDate.now().toString(); searchQuery = ""; ventasViewModel.refreshVentas(fechaActiva, corteCajaActivo) }
                        )
                    }

                    composable(ScreenRoutes.NUEVA_VENTA) { backStackEntry ->
                        val nuevaVentaViewModel: NuevaVentaViewModel = viewModel(factory = factory)
                        val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                        val cajas by sharedCajaViewModel.cajas.collectAsState()
                        val cajaActiva = cajas.find { it.id == selectedCajaId }

                        val tipoVenta by nuevaVentaViewModel.tipoVenta.collectAsState()
                        val nombreCliente by nuevaVentaViewModel.nombreCliente.collectAsState()
                        val socioId by nuevaVentaViewModel.socioId.collectAsState()
                        val tiposDeVenta by nuevaVentaViewModel.tiposVenta.collectAsState()
                        val searchQuery by nuevaVentaViewModel.searchQuery.collectAsState()
                        val sociosEncontrados by nuevaVentaViewModel.sociosEncontrados.collectAsState()
                        val socioSeleccionado by nuevaVentaViewModel.socioSeleccionado.collectAsState()

                        LaunchedEffect(tipoVenta, nombreCliente, cajaActiva?.corte) {
                            // Si el carrito ya tiene items, no sobreescribimos la config de la venta
                            // a menos que estemos en modo append explícito
                            if (globalCarritoViewModel.items.value.isEmpty() || globalCarritoViewModel.esModoAppend()) {
                                globalCarritoViewModel.configurarVenta(tipoVenta, socioId, nombreCliente, cajaActiva?.corte ?: 0, cajaActiva?.clavePuntoVenta ?: "")
                            }
                        }

                        NuevaVentaConfigScreen(
                            tiposDeVenta = tiposDeVenta,
                            tipoSeleccionado = tipoVenta,
                            onTipoVentaChange = { nuevaVentaViewModel.setTipoVenta(it) },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { nuevaVentaViewModel.searchSocios(it) },
                            sociosEncontrados = sociosEncontrados,
                            onSocioSeleccionado = { if (it != null) nuevaVentaViewModel.selectSocio(it) else nuevaVentaViewModel.clearSocioSelection() },
                            socioSeleccionado = socioSeleccionado,
                            nombreCliente = nombreCliente,
                            onNombreClienteChange = { nuevaVentaViewModel.setNombreCliente(it) },
                            isOnline = isConnected,
                            onMenuClick = { navController.popBackStack() },
                            onContinuarClick = { navController.navigate(ScreenRoutes.BUSCAR_PRODUCTOS) },
                            cajasDisponibles = cajas.isNotEmpty(),
                            isFormValid = nuevaVentaViewModel.isFormValid()
                        )
                    }

                    composable(ScreenRoutes.BUSCAR_PRODUCTOS) { backStackEntry ->
                        val productos by globalCarritoViewModel.productos.collectAsState()
                        val searchQuery by globalCarritoViewModel.searchQuery.collectAsState()
                        val items by globalCarritoViewModel.items.collectAsState()

                        BuscarProductosScreen(
                            productos = productos,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { globalCarritoViewModel.searchProductos(it) },
                            carritoCount = items.size,
                            onAddProducto = { p, c, o -> globalCarritoViewModel.addProducto(p, c, o) },
                            onProductoConModificadores = { p, c -> globalCarritoViewModel.seleccionarProducto(p, c); navController.navigate(ScreenRoutes.crearRutaModificadores(p.id)) },
                            onVerCarrito = { navController.navigate(ScreenRoutes.RESUMEN_CARRITO) },
                            onBackClick = { navController.popBackStack() },
                            isOnline = isConnected
                        )
                    }

                    composable(route = ScreenRoutes.MODIFICADORES, arguments = listOf(navArgument("productoId") { type = NavType.IntType })) { backStackEntry ->
                        val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
                        val modVM: ModificadoresViewModel = viewModel(factory = factory)
                        val productos by globalCarritoViewModel.productos.collectAsState()
                        val grupos by modVM.grupos.collectAsState()
                        val modificadores by modVM.modificadores.collectAsState()

                        LaunchedEffect(productoId) { modVM.cargarModificadores(productoId) }

                        val producto = productos.find { it.id == productoId }
                        if (producto != null) {
                            SeleccionarModificadoresScreen(
                                producto = producto,
                                gruposModificadores = grupos,
                                modificadoresDisponibles = modificadores,
                                cantidadProducto = globalCarritoViewModel.cantidadSeleccionada,
                                onAddToCart = { m, o, mn -> 
                                    globalCarritoViewModel.addProductoConModificadores(producto, m, grupos, globalCarritoViewModel.cantidadSeleccionada, o, mn)
                                    navController.popBackStack() 
                                },
                                onBackClick = { navController.popBackStack() },
                                isOnline = isConnected
                            )
                        }
                    }

                    composable(ScreenRoutes.RESUMEN_CARRITO) { backStackEntry ->
                        val items by globalCarritoViewModel.items.collectAsState()
                        val tipoVenta by globalCarritoViewModel.tipoVenta.collectAsState()
                        val nombreCliente by globalCarritoViewModel.nombreCliente.collectAsState()
                        val corteCaja by globalCarritoViewModel.corteCaja.collectAsState()
                        val clavePuntoVenta by globalCarritoViewModel.clavePuntoVenta.collectAsState()
                        val total by globalCarritoViewModel.total.collectAsState()
                        val isSending by globalCarritoViewModel.isSending.collectAsState()
                        val sendResult by globalCarritoViewModel.sendResult.collectAsState()

                        LaunchedEffect(sendResult) {
                            if (sendResult is SendResult.Success) {
                                delay(2000L)
                                // Limpiamos el carrito global solo al éxito
                                globalCarritoViewModel.clearState()
                                // Volvemos a la pantalla de ventas de forma segura
                                navController.popBackStack(ScreenRoutes.VENTAS, inclusive = false)
                            }
                        }

                        ResumenCarritoScreen(
                            items = items,
                            tipoVenta = tipoVenta,
                            isAppend = globalCarritoViewModel.esModoAppend(),
                            nombreCliente = nombreCliente,
                            corteCaja = corteCaja,
                            clavePuntoVenta = clavePuntoVenta,
                            total = total,
                            isSending = isSending,
                            sendResult = sendResult,
                            onUpdateCantidad = { i, c -> globalCarritoViewModel.updateCantidad(i, c) },
                            onRemoveItem = { globalCarritoViewModel.removeProducto(it) },
                            onDeshacer = { i, idx -> globalCarritoViewModel.insertarProducto(idx, i) },
                            onConfirmar = { globalCarritoViewModel.confirmarVenta() },
                            onVolver = {
                                globalCarritoViewModel.clearState()
                                navController.popBackStack(ScreenRoutes.VENTAS, inclusive = false)
                            },
                            onBackClick = { navController.popBackStack() },
                            isOnline = isConnected
                        )
                    }

                    composable(route = ScreenRoutes.DETALLE_VENTA, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val ventasViewModel: VentasViewModel = viewModel(factory = factory)
                        var ventaDetalle by remember { mutableStateOf<VentaDto?>(null) }
                        var productoATransferir by remember { mutableStateOf<ProductoVentaDto?>(null) }
                        val ventasAbiertas by ventasViewModel.getVentasAbiertasDelCorte(ventaDetalle?.cajaId ?: 0).collectAsState(initial = emptyList())
                        val coroutineScope = rememberCoroutineScope()

                        LaunchedEffect(id) { 
                            ventaDetalle = ventasViewModel.cargarDetalle(id) 
                        }

                        val actualFolio = ventaDetalle?.folio ?: 0

                        productoATransferir?.let { prod ->
                            TransferirProductoDialog(prod, ventasAbiertas.filter { it.folio != actualFolio }, { folioDestino ->
                                ventasViewModel.transferirProducto(actualFolio, prod.chunk, folioDestino) {
                                    if (it.isSuccess) coroutineScope.launch { ventaDetalle = ventasViewModel.cargarDetalle(id) }
                                    productoATransferir = null
                                }
                            }, { productoATransferir = null })
                        }

                        DetalleVentaScreen(
                            venta = ventaDetalle,
                            isLoading = ventaDetalle == null,
                            isOnline = isConnected,
                            onBackClick = { navController.popBackStack() },
                            onAgregarProductos = {
                                val v = ventaDetalle
                                if (v != null) {
                                    globalCarritoViewModel.clearState()
                                    // Configuramos append mode con el idTemporal si existe (para ventas locales)
                                    // o con el folio (para ventas recibidas)
                                    globalCarritoViewModel.configurarAppendMode(
                                        folio = v.folio,
                                        nombreCliente = v.nombreCliente,
                                        clavePuntoVenta = v.clavePuntoVenta,
                                        tipoVenta = v.tipoCliente ?: "",
                                        corteCaja = v.cajaId ?: 0,
                                        idTemporal = v.idTemporal
                                    )
                                    navController.navigate(ScreenRoutes.BUSCAR_PRODUCTOS)
                                }
                            },
                            onTransferirProducto = null
                        )
                    }
                }

                navigation(startDestination = ScreenRoutes.SOCIOS, route = NavGraphs.SOCIOS_GRAPH) {
                    composable(ScreenRoutes.SOCIOS) { backStackEntry ->
                        val sociosVM: SociosViewModel = viewModel(viewModelStoreOwner = remember(backStackEntry) { navController.getBackStackEntry(NavGraphs.SOCIOS_GRAPH) }, factory = factory)
                        val socios by sociosVM.socios.collectAsState()
                        val isLoading by sociosVM.isLoading.collectAsState()
                        val searchQuery by sociosVM.searchQuery.collectAsState()
                        val errorMessage by sociosVM.error.collectAsState()
                        val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                        val cajas by sharedCajaViewModel.cajas.collectAsState()
                        val cajaActiva = cajas.find { it.id == selectedCajaId }

                        SociosScreen(
                            socios = socios,
                            isLoading = isLoading,
                            isOnline = viewModel?.isOnline ?: true,
                            searchQuery = searchQuery,
                            nombreCaja = cajaActiva?.nombre ?: "Sin Caja",
                            errorMessage = errorMessage,
                            onSearchQueryChange = { sociosVM.search(it) },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSocioClick = { navController.navigate(ScreenRoutes.crearRutaPerfilSocio(it)) },
                            onRetry = { sociosVM.sync() },
                            onRefresh = { sociosVM.search(""); sociosVM.sync() }
                        )
                    }
                    composable(route = ScreenRoutes.PERFIL_SOCIO, arguments = listOf(navArgument("socioId") { type = NavType.IntType })) { backStackEntry ->
                        val socioId = backStackEntry.arguments?.getInt("socioId") ?: 0
                        val sociosVM: SociosViewModel = viewModel(viewModelStoreOwner = remember(backStackEntry) { navController.getBackStackEntry(NavGraphs.SOCIOS_GRAPH) }, factory = factory)
                        val socio by sociosVM.selectedSocio.collectAsState()
                        val integrantes by sociosVM.integrantes.collectAsState(initial = emptyList())

                        LaunchedEffect(socioId) { sociosVM.getIntegrantesPorSocio(socioId) }

                        val s = socio
                        if (s != null) PerfilSocioScreen(s, integrantes, viewModel?.isOnline ?: true) { navController.popBackStack() }
                        else Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = VerdePrincipal) }
                    }
                }

                navigation(startDestination = ScreenRoutes.AJUSTES, route = NavGraphs.AJUSTES_GRAPH) {
                    composable(ScreenRoutes.AJUSTES) {
                        val cajas by sharedCajaViewModel.cajas.collectAsState()
                        val selectedCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
                        val isLoading by sharedCajaViewModel.isLoading.collectAsState()

                        LaunchedEffect(currentRoute) { if (currentRoute == ScreenRoutes.AJUSTES) sharedCajaViewModel.refreshCajas() }
                        val sessionManager = SessionManager.getInstance(context)
                        val lastSyncText = if (sessionManager.getLastSyncDate() > 0) java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(sessionManager.getLastSyncDate())) else "Pendiente"
                        AjustesScreen(
                            cajas = cajas.map { CajaDto(it.id, it.nombre, it.fechaApertura, it.fechaCierre, it.activo, it.meseroId) },
                            selectedCajaId = selectedCajaId,
                            lastSyncDate = lastSyncText,
                            isLoading = isLoading,
                            isOnline = viewModel?.isOnline ?: true,
                            themeMode = viewModel?.themeMode ?: 0,
                            onThemeModeChange = { viewModel?.updateThemeMode(it) },
                            onCajaClick = { sharedCajaViewModel.selectCaja(it.id, it.nombre) },
                            onSyncClick = { SyncWorker.enqueueOneTime(context); android.widget.Toast.makeText(context, "Sincronizando...", android.widget.Toast.LENGTH_SHORT).show() },
                            onLogoutClick = { onLogout() },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onRefresh = { scope.launch { sharedCajaViewModel.refreshCajas() } }
                        )
                    }
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