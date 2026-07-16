package com.example.sisvvapp.ui.screens.main

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.sync.SyncEventBus
import com.example.sisvvapp.data.sync.SyncForegroundService
import com.example.sisvvapp.data.sync.SyncWorker
import com.example.sisvvapp.network.RetrofitClient
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.ventas.ProductoVentaDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.AppNavigationDrawerContent
import com.example.sisvvapp.ui.components.TransferirProductoDialog
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.navigation.NavGraphs
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.screens.ajustes.AjustesScreen
import com.example.sisvvapp.ui.screens.socios.PerfilSocioScreen
import com.example.sisvvapp.ui.screens.socios.SociosScreen
import com.example.sisvvapp.ui.screens.ventas.*
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.utils.LocalIsConnected
import com.example.sisvvapp.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainContainer(
    viewModel: SisvvViewModel? = null,
    onLogout: () -> Unit = {},
    onCajaClosed: () -> Unit = {}
) {
    val context = LocalContext.current
    val factory = SisvvViewModelFactory(context)
    val sharedCajaViewModel: CajaViewModel = viewModel(factory = factory)

    val snackbarHostState = remember { SnackbarHostState() }
    val isConnected = LocalIsConnected.current

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ViewModel persistente para el flujo de ventas
    val globalCarritoViewModel: CarritoViewModel = viewModel(factory = factory)

    var showCajaCerradaDialog by remember { mutableStateOf(false) }
    var motivoCajaCerrada by remember { mutableStateOf("") }
    var hadCajas by remember { mutableStateOf(false) }
    val isSessionExpired = remember { derivedStateOf { !isConnected && !com.example.sisvvapp.data.local.SessionManager.getInstance(context).isLoggedIn() } }
    
    // Conjunto para evitar spam de alertas de la misma caja
    val cajasNotificadas = remember { mutableSetOf<Int>() }

    // Sincronización al montar la pantalla (app reabierta o primer inicio)
    LaunchedEffect(Unit) {
        SyncForegroundService.start(context)
        SyncWorker.enqueueOneTime(context)
    }

    // Sincronización automática al recuperar conexión
    LaunchedEffect(isConnected) {
        if (isConnected) {
            sharedCajaViewModel.refreshCajas()
            SyncWorker.enqueueOneTime(context)
        }
    }

    // Detectar caja cerrada durante sync en segundo plano (venta offline falló)
    LaunchedEffect(Unit) {
        SyncEventBus.events.collect { event ->
            if (event is SyncEventBus.SyncEvent.CajaCerrada && !showCajaCerradaDialog) {
                // SOLO mostramos si la sesión aún es válida (prioridad)
                val session = com.example.sisvvapp.data.local.SessionManager.getInstance(context)
                if (session.isLoggedIn()) {
                    motivoCajaCerrada = "venta_offline"
                    showCajaCerradaDialog = true
                }
            }
        }
    }

    // Detectar cuando todas las cajas se cerraron desde la web
    val topCajas by sharedCajaViewModel.cajas.collectAsState()
    val topCajaId by sharedCajaViewModel.selectedCajaId.collectAsState()
    val isLoadingCajas by sharedCajaViewModel.isLoading.collectAsState()
    LaunchedEffect(topCajas) {
        if (topCajas.isNotEmpty()) hadCajas = true
    }
    LaunchedEffect(topCajas, topCajaId, isLoadingCajas) {
        if (topCajas.isEmpty() && topCajaId != null && hadCajas && !isLoadingCajas && !showCajaCerradaDialog) {
            motivoCajaCerrada = "cajas_vacias"
            showCajaCerradaDialog = true
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoutes.VENTAS

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
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
                    viewModel = viewModel
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
                        val cajaActiva by remember(selectedCajaId, cajas) {
                            derivedStateOf { cajas.find { it.id == selectedCajaId } }
                        }
                        val nombreCajaActiva by remember(cajaActiva) {
                            derivedStateOf { cajaActiva?.nombre ?: "Sin Caja" }
                        }
                        val corteCajaActivo by remember(cajaActiva) {
                            derivedStateOf { cajaActiva?.corte }
                        }
                        var searchQuery by remember { mutableStateOf("") }
                        var fechaActiva by remember { mutableStateOf("") }

                        LaunchedEffect(selectedCajaId, corteCajaActivo, isConnected) {
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

                        // Refrescar ventas al reanudar la app (vuelta de web, etc.)
                        val lifecycleOwner = LocalLifecycleOwner.current
                        DisposableEffect(lifecycleOwner) {
                            val observer = LifecycleEventObserver { _, event ->
                                if (event == Lifecycle.Event.ON_RESUME) {
                                    val today = java.time.LocalDate.now().toString()
                                    fechaActiva = today
                                    ventasViewModel.refreshVentas(today, corteCajaActivo)
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                            onVentasPendientesClick = { navController.navigate(ScreenRoutes.VENTAS_PENDIENTES) },
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

                    composable(
                        route = ScreenRoutes.NUEVA_VENTA_CON_ARG,
                        arguments = listOf(navArgument("socioId") { type = NavType.IntType; defaultValue = -1 })
                    ) { backStackEntry ->
                        val nuevaVentaViewModel: NuevaVentaViewModel = viewModel(factory = factory)
                        val socioIdArg = backStackEntry.arguments?.getInt("socioId") ?: -1
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
                        val numeroComensales by nuevaVentaViewModel.numeroComensales.collectAsState()
                        val paraLlevar by nuevaVentaViewModel.paraLlevar.collectAsState()

                        LaunchedEffect(socioIdArg) {
                            if (socioIdArg != -1) {
                                nuevaVentaViewModel.setRestrictedMode(true)
                                nuevaVentaViewModel.selectSocioById(socioIdArg)
                            } else {
                                nuevaVentaViewModel.setRestrictedMode(false)
                            }
                        }

                        LaunchedEffect(tipoVenta, nombreCliente, cajaActiva?.corte, numeroComensales, paraLlevar, socioId) {
                            val comensalesFinal = if (paraLlevar) "PARA LLEVAR" else numeroComensales
                            globalCarritoViewModel.configurarVenta(tipoVenta, socioId, nombreCliente, cajaActiva?.corte ?: 0, cajaActiva?.clavePuntoVenta ?: "", comensalesFinal)
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
                            numeroComensales = numeroComensales,
                            onNumeroComensalesChange = { nuevaVentaViewModel.setNumeroComensales(it) },
                            paraLlevar = paraLlevar,
                            onParaLlevarChange = { nuevaVentaViewModel.setParaLlevar(it) },
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
                            val result = sendResult
                            when (result) {
                                is SendResult.Success -> {
                                    globalCarritoViewModel.clearState()
                                    val popped = navController.popBackStack(ScreenRoutes.VENTAS, inclusive = false)
                                    if (!popped) {
                                        navController.navigate(ScreenRoutes.VENTAS) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Venta realizada con éxito (Folio: ${result.folio})",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                is SendResult.Offline -> {
                                    globalCarritoViewModel.clearState()
                                    val popped = navController.popBackStack(ScreenRoutes.VENTAS, inclusive = false)
                                    if (!popped) {
                                        navController.navigate(ScreenRoutes.VENTAS) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Venta guardada, se sincronizará al reconectar",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                else -> {}
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
                                val popped = navController.popBackStack(ScreenRoutes.VENTAS, inclusive = false)
                                if (!popped) {
                                    navController.navigate(ScreenRoutes.VENTAS) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
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
                            onDescartarVenta = { idTemporal ->
                                ventasViewModel.descartarVentaPendiente(idTemporal) { result ->
                                    if (result.isSuccess) {
                                        android.widget.Toast.makeText(context, "Venta descartada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onTransferirProducto = null
                        )
                    }

                    composable(ScreenRoutes.VENTAS_PENDIENTES) {
                        val pendientesVM: VentasPendientesViewModel = viewModel(factory = factory)
                        val ventas by pendientesVM.ventasPendientes.collectAsState()
                        
                        VentasPendientesScreen(
                            ventas = ventas,
                            onBackClick = { navController.popBackStack() },
                            onDescartar = { id ->
                                pendientesVM.descartarVenta(id) { result ->
                                    if (result.isSuccess) {
                                        android.widget.Toast.makeText(context, "Venta descartada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onPausarSync = { pendientesVM.pausarSincronizacion() },
                            onReanudarSync = { pendientesVM.reanudarSincronizacion() }
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
                            onNuevaVentaClick = { navController.navigate(ScreenRoutes.crearRutaNuevaVenta(it.id)) },
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
                        if (s != null) PerfilSocioScreen(s, integrantes, viewModel?.isOnline ?: true, onNuevaVentaClick = { navController.navigate(ScreenRoutes.crearRutaNuevaVenta(it.id)) }) { navController.popBackStack() }
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
                        var currentBaseUrl by remember { mutableStateOf(sessionManager.getBaseUrl()) }
                        AjustesScreen(
                            cajas = cajas.map { CajaDto(it.id, it.nombre, it.fechaApertura, it.fechaCierre, it.activo, it.meseroId) },
                            selectedCajaId = selectedCajaId,
                            lastSyncDate = lastSyncText,
                            isLoading = isLoading,
                            isOnline = viewModel?.isOnline ?: true,
                            themeMode = viewModel?.themeMode ?: 0,
                            baseUrl = currentBaseUrl,
                            onThemeModeChange = { viewModel?.updateThemeMode(it) },
                            onBaseUrlChange = { newUrl ->
                                currentBaseUrl = newUrl
                                RetrofitClient.updateBaseUrl(context, newUrl)
                                android.widget.Toast.makeText(context, "URL actualizada. La app usará la nueva URL en la próxima conexión.", android.widget.Toast.LENGTH_LONG).show()
                            },
                            onCajaClick = { sharedCajaViewModel.selectCaja(it.id, it.nombre) },
                            onVentasPendientesClick = { navController.navigate(ScreenRoutes.VENTAS_PENDIENTES) },
                            onSyncClick = { SyncWorker.enqueueOneTime(context); android.widget.Toast.makeText(context, "Sincronizando...", android.widget.Toast.LENGTH_SHORT).show() },
                            onLogoutClick = { onLogout() },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onRefresh = { scope.launch { sharedCajaViewModel.refreshCajas() } }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
        ) { data ->
            val icon = when {
                data.visuals.message.contains("éxito") -> Icons.Default.CheckCircle
                data.visuals.message.contains("sincronizará") -> Icons.Default.CloudQueue
                else -> Icons.Default.Sync
            }
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
                                imageVector = icon,
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

        if (showCajaCerradaDialog) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Caja cerrada",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (motivoCajaCerrada == "cajas_vacias") {
                            Text(
                                text = "Todas las cajas fueron cerradas.\n\n" +
                                        "No hay cajas activas disponibles. Reintenta más tarde o cuando se abra una nueva caja.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "La caja seleccionada fue cerrada en otro dispositivo.\n\n" +
                                        "Las ventas offline pendientes se descartaron porque ya no pueden sincronizarse.\n\n" +
                                        "Selecciona una caja activa para continuar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (motivoCajaCerrada == "cajas_vacias") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showCajaCerradaDialog = false
                                        onLogout()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cerrar sesión")
                                }
                                VistaVerdeButton(
                                    text = "Reintentar",
                                    onClick = {
                                        showCajaCerradaDialog = false
                                        scope.launch { sharedCajaViewModel.refreshCajas() }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            VistaVerdeButton(
                                text = "Seleccionar otra caja",
                                onClick = {
                                    showCajaCerradaDialog = false
                                    sharedCajaViewModel.clearSelectedCaja()
                                    onCajaClosed()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
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