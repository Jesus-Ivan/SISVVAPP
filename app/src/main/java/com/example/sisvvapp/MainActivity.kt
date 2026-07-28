package com.example.sisvvapp

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sisvvapp.data.monitor.NetworkMonitor
import com.example.sisvvapp.data.repository.toCajaDto
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.sync.SyncWorker
import com.example.sisvvapp.network.RetrofitClient
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.navigation.Screen
import com.example.sisvvapp.ui.screens.caja.CajaScreen
import com.example.sisvvapp.ui.screens.login.LoginScreen
import com.example.sisvvapp.ui.screens.main.MainContainer
import com.example.sisvvapp.ui.screens.splash.SplashScreen
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import com.example.sisvvapp.ui.utils.LocalIsConnected
import com.example.sisvvapp.ui.viewmodel.CajaViewModel
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Forzamos vertical antes de cualquier otra cosa
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Programar sync periódico
        SyncWorker.enqueuePeriodic(this)

        val app = application as SisvvApplication

        setContent {
            val sisvvViewModel: SisvvViewModel = viewModel(
                factory = SisvvViewModelFactory(this)
            )

            val isDarkTheme = when (sisvvViewModel.themeMode) {
                1 -> false // Claro
                2 -> true  // Oscuro
                else -> isSystemInDarkTheme() // Sistema (0)
            }

            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkMonitor = remember { NetworkMonitor(connectivityManager) }
            val isConnected by networkMonitor.isConnected.collectAsState(initial = true)

            val configuration = LocalConfiguration.current
            val deviceType =
                if (configuration.smallestScreenWidthDp >= 600) {
                    DeviceType.TABLET
                } else {
                    DeviceType.MOBILE
                }

            SISVVAPPTheme(darkTheme = isDarkTheme, isTablet = deviceType == DeviceType.TABLET) {
                val navController = rememberNavController()

                // Lógica de Permisos de Notificaciones
                var showPermissionRationale by remember { mutableStateOf(false) }
                var showSessionExpiredDialog by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (!isGranted) {
                            showPermissionRationale = true
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                if (showPermissionRationale) {
                    Dialog(onDismissRequest = { showPermissionRationale = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Permiso de Notificaciones",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Las notificaciones son necesarias para asegurar que tus ventas se sincronicen correctamente con el servidor en segundo plano.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                VistaVerdeButton(
                                    text = "Entendido",
                                    onClick = {
                                        showPermissionRationale = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = { showPermissionRationale = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Ahora no",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                if (showSessionExpiredDialog) {
                    Dialog(onDismissRequest = { /* No permitir cerrar fuera */ }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Sesión Expirada / Caja Cerrada",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Tu sesión ha expirado por seguridad o la caja ha sido cerrada.\n\n" +
                                            "Las ventas locales pendientes han sido descartadas ya que no pueden sincronizarse.\n\n" +
                                            "Por favor, inicia sesión de nuevo para continuar.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                VistaVerdeButton(
                                    text = "Ir al Login",
                                    onClick = {
                                        showSessionExpiredDialog = false
                                        sisvvViewModel.resetLoginStatus() // Doble seguridad
                                        WorkManager.getInstance(this@MainActivity).cancelAllWork()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                val logoutSuccess = sisvvViewModel.logoutSuccess
                LaunchedEffect(logoutSuccess) {
                    if (logoutSuccess) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        sisvvViewModel.resetLogoutStatus()
                    }
                }

                LaunchedEffect(Unit) {
                    app.unauthorizedEvent.collect {
                        Log.d("MainActivity", "Sesión expirada detectada, limpiando y mostrando diálogo")
                        // Limpieza inmediata para romper el bucle
                        com.example.sisvvapp.data.local.SessionManager.getInstance(this@MainActivity).clearSession()
                        sisvvViewModel.resetLoginStatus()
                        showSessionExpiredDialog = true
                    }
                }

                CompositionLocalProvider(
                    LocalDeviceType provides deviceType,
                    LocalIsConnected provides isConnected
                ) {
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
                            val sessionManager = SessionManager.getInstance(this@MainActivity)
                            var currentBaseUrl by remember { mutableStateOf(sessionManager.getBaseUrl()) }

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
                                baseUrl = currentBaseUrl,
                                onBaseUrlChange = { newUrl ->
                                    currentBaseUrl = newUrl
                                    RetrofitClient.updateBaseUrl(this@MainActivity, newUrl)
                                },
                                onLoginClick = { email, password ->
                                    sisvvViewModel.login(email, password)
                                }
                            )
                        }

                        // --- 2.1 PANTALLA DE SELECCIÓN DE CAJA INICIAL (POST-LOGIN OBLIGATORIO) ---
                        composable(Screen.CajaInicial.route) {
                            val cajaViewModel: CajaViewModel = viewModel(factory = SisvvViewModelFactory(this@MainActivity))
                            val scope = rememberCoroutineScope()

                            val cajas by cajaViewModel.cajas.collectAsState()
                            val selectedCajaId by cajaViewModel.selectedCajaId.collectAsState()
                            val isLoading by cajaViewModel.isLoading.collectAsState()

                            val cajasDto = cajas.map { it.toCajaDto() }

                            CajaScreen(
                                cajas = cajasDto,
                                selectedCajaId = selectedCajaId,
                                isLoading = isLoading,
                                isOnline = sisvvViewModel.isOnline,
                                isFromSettings = false,
                                onCajaClick = { id -> cajaViewModel.selectCaja(id) },
                                onNavigationClick = {},
                                onRetry = { scope.launch { cajaViewModel.refreshCajas() } },
                                onContinueClick = { id ->
                                    val caja = cajas.firstOrNull { it.id == id }
                                    val sessionManager = com.example.sisvvapp.data.local.SessionManager.getInstance(this@MainActivity)
                                    sessionManager.saveSelectedCaja(id, caja?.nombre ?: "")
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- 3. CONTENEDOR PRINCIPAL (Ventas, Socios, Ajustes) ---
                        composable(Screen.Main.route) {
                            MainContainer(
                                viewModel = sisvvViewModel,
                                onLogout = {
                                    sisvvViewModel.logout()
                                },
                                onCajaClosed = {
                                    navController.navigate(Screen.CajaInicial.route) {
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