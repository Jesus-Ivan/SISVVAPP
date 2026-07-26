package com.example.sisvvapp.ui.screens.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.CambiarUrlDialog
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
    pendientesCount: Int = 0,
    onThemeModeChange: (Int) -> Unit = {},
    onBaseUrlChange: (String) -> Unit = {},
    onCajaClick: (CajaDto) -> Unit,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onVentasPendientesClick: () -> Unit = {},
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

                // --- 2.5 SECCIÓN: COLA DE SINCRONIZACIÓN (Nueva) ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeSectionHeader(text = "Operaciones Fuera de Línea")
                }

                item {
                    VistaVerdeBaseCard(
                        modifier = Modifier.fillMaxWidth().clickable { onVentasPendientesClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (pendientesCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(if (isTablet) 56.dp else 48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = if (pendientesCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cola de Sincronización",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (pendientesCount > 0) "$pendientesCount pedidos esperando conexión" else "No hay pedidos pendientes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
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
                                onClick = { showDialog = true },
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
                        CambiarUrlDialog(
                            currentUrl = baseUrl,
                            onDismiss = { showDialog = false },
                            onConfirm = { newUrl ->
                                onBaseUrlChange(newUrl)
                                showDialog = false
                            }
                        )
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