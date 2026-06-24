package com.example.sisvvapp.ui.screens.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sisvvapp.R
import com.example.sisvvapp.network.RetrofitClient
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeTextField
import com.example.sisvvapp.ui.components.ThemeOptionSmall
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.components.VistaVerdeSkeletonCard
import com.example.sisvvapp.ui.screens.caja.VistaVerdeCajaCard
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType

@Composable
fun AjustesScreen(
    cajas: List<CajaDto>,
    selectedCajaId: Int?,
    lastSyncDate: String,
    isLoading: Boolean = false,
    isOnline: Boolean = true,
    themeMode: Int = 0,
    baseUrl: String = "",
    onThemeModeChange: (Int) -> Unit = {},
    onBaseUrlChange: (String) -> Unit = {},
    onCajaClick: (CajaDto) -> Unit,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET

    VistaVerdeScaffold(
        title = stringResource(id = R.string.ajustes_title),
        subtitle = stringResource(id = R.string.ajustes_subtitle),
        onMenuClick = onMenuClick,
        isOnline = isOnline,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar cajas")
            }
        }
    ) {
        ResponsiveContainer {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- 1. SECCIÓN: CAJAS ABIERTAS ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VistaVerdeSectionHeader(text = stringResource(id = R.string.ajustes_cajas_abiertas))
                    }
                }

                // LÓGICA DE ESTADOS MODIFICADA
                if (isLoading) {
                    // ESTADO 1: Cargando
                    items(3) {
                        VistaVerdeSkeletonCard()
                    }
                } else if (cajas.isEmpty()) {
                    // ESTADO 2: Lista vacía
                    item {
                        VistaVerdeEmptyState(
                            icon = Icons.Default.Inbox,
                            message = stringResource(R.string.ajustes_cajas_empty),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                } else {
                    // ESTADO 3: Lista real de cajas
                    items(items = cajas, key = { it.id }) { caja ->
                        VistaVerdeCajaCard(
                            caja = caja,
                            isSelected = caja.id == selectedCajaId,
                            onClick = { onCajaClick(caja) }
                        )
                    }
                }

                // --- 2. SECCIÓN: SINCRONIZAR DATOS ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeSectionHeader(text = stringResource(id = R.string.ajustes_sincronizar_datos))
                }

                item {
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isTablet) 20.dp else 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.ajustes_sync_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Button(
                                onClick = onSyncClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(id = R.string.ajustes_btn_actualizar),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.ajustes_btn_actualizar),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isTablet) 17.sp else 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(id = R.string.ajustes_ultima_act, lastSyncDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = if (isTablet) 22.sp else 18.sp
                            )
                        }
                    }
                }

                // --- 3. SECCIÓN: CONFIGURACIÓN DEL SERVIDOR ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeSectionHeader(text = "Configuración del servidor")
                }

                item {
                    var showDialog by remember { mutableStateOf(false) }
                    var editUrl by remember { mutableStateOf("") }

                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isTablet) 20.dp else 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "URL del servidor",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = baseUrl,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = com.example.sisvvapp.ui.theme.Inter
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    editUrl = baseUrl
                                    showDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Cambiar URL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isTablet) 17.sp else 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    if (showDialog) {
                        var isVerifying by remember { mutableStateOf(false) }
                        var verifyError by remember { mutableStateOf<String?>(null) }
                        val scope = rememberCoroutineScope()

                        Dialog(onDismissRequest = { if (!isVerifying) { showDialog = false; verifyError = null } }) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.94f)
                                    .padding(vertical = 24.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Dns,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Cambiar URL del servidor",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    OutlinedTextField(
                                        value = editUrl,
                                        onValueChange = { newVal ->
                                            val apiSuffix = "/api/"
                                            editUrl = if (!newVal.endsWith(apiSuffix) && newVal.length < editUrl.length) {
                                                newVal + apiSuffix
                                            } else {
                                                newVal
                                            }
                                            verifyError = null
                                        },
                                        label = { Text("URL Base") },
                                        placeholder = { Text("http://192.168.0.101/api/") },
                                        singleLine = true,
                                        enabled = !isVerifying,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            cursorColor = MaterialTheme.colorScheme.primary,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = com.example.sisvvapp.ui.theme.Inter
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (verifyError != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = verifyError!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showDialog = false; verifyError = null },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(if (isTablet) 64.dp else 56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            enabled = !isVerifying
                                        ) {
                                            Text(
                                                "Cancelar",
                                                fontWeight = FontWeight.Medium,
                                                fontSize = if (isTablet) 15.sp else 14.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (isVerifying) return@Button
                                                isVerifying = true
                                                verifyError = null
                                                        scope.launch {
                                                    val result = RetrofitClient.testConnection(editUrl)
                                                    if (result.isSuccess) {
                                                        onBaseUrlChange(editUrl)
                                                        showDialog = false
                                                    } else {
                                                        val msg = result.exceptionOrNull()?.message?.lowercase() ?: ""
                                                        verifyError = when {
                                                            msg.contains("timeout") -> "No se pudo conectar: tiempo de espera agotado"
                                                            msg.contains("unable to resolve host") || msg.contains("dns") ->
                                                                "No se pudo conectar: el host no existe. Verifica la URL"
                                                            msg.contains("failed to connect") || msg.contains("connection refused") ->
                                                                "No se pudo conectar: servidor rechazó la conexión"
                                                            msg.contains("network is unreachable") ->
                                                                "No se pudo conectar: red no disponible"
                                                            else -> "No se pudo conectar: ${result.exceptionOrNull()?.message}"
                                                        }
                                                        isVerifying = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(if (isTablet) 64.dp else 56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            enabled = !isVerifying
                                        ) {
                                            if (isVerifying) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            } else {
                                                Text(
                                                    text = "Guardar",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = if (isTablet) 15.sp else 14.sp
                                                )
                                            }
                                        }
                                    }

                                    if (isVerifying) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LinearProgressIndicator(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 4. SECCIÓN: APARIENCIA ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeSectionHeader(text = "Apariencia")
                }

                item {
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isTablet) 20.dp else 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionSmall(
                                icon = Icons.Default.AutoMode,
                                selected = themeMode == 0,
                                onClick = { onThemeModeChange(0) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionSmall(
                                icon = Icons.Default.LightMode,
                                selected = themeMode == 1,
                                onClick = { onThemeModeChange(1) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionSmall(
                                icon = Icons.Default.DarkMode,
                                selected = themeMode == 2,
                                onClick = { onThemeModeChange(2) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // --- 5. SECCIÓN: CERRAR SESIÓN ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(0.dp, MaterialTheme.colorScheme.surface),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(id = R.string.ajustes_btn_cerrar_sesion),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.ajustes_btn_cerrar_sesion),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isTablet) 18.sp else 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AjustesScreenPreview() {
    val mockCajas = listOf(
        CajaDto(6858, "Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123)
    )

    SISVVAPPTheme {
        AjustesScreen(
            cajas = mockCajas,
            selectedCajaId = 6858,
            lastSyncDate = "Hoy 11:24 AM",
            onCajaClick = {},
            onSyncClick = {},
            onLogoutClick = {},
            onMenuClick = {}
        )
    }
}