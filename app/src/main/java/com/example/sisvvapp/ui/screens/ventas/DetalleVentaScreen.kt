package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.network.dto.ventas.ProductoVentaDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.Poppins
import java.util.Locale

@Composable
fun DetalleVentaScreen(
    venta: VentaDto?,
    isLoading: Boolean,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onAgregarProductos: () -> Unit,
    onReimprimir: () -> Unit = {},
    onTransferirProducto: ((ProductoVentaDto) -> Unit)? = null
) {
    val syncStatus = venta?.syncStatus ?: "RECIBIDA"
    val isSyncing = syncStatus == "SYNCING"
    val deviceType = com.example.sisvvapp.ui.utils.LocalDeviceType.current
    val isTablet = deviceType == com.example.sisvvapp.ui.utils.DeviceType.TABLET
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarShown = remember { mutableStateOf(false) }

    val tienePendientes = venta?.productos?.any { it.idEstado == "0" || it.idEstado == "3" } == true

    LaunchedEffect(tienePendientes, venta, isOnline) {
        if (tienePendientes && !snackbarShown.value && isOnline) {
            snackbarShown.value = true
            val result = snackbarHostState.showSnackbar(
                message = "Hay productos pendientes de impresión",
                actionLabel = "REIMPRIMIR",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                snackbarShown.value = true
                onReimprimir()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VistaVerdeScaffold(
            title = "Detalle de Venta",
            onMenuClick = onBackClick,
            isBackButton = true,
            isOnline = if (isSyncing) true else isOnline
        ) {
            ResponsiveContainer {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (venta == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        VistaVerdeEmptyState(
                            icon = Icons.Filled.ShoppingCart,
                            message = "Venta no encontrada"
                        )
                    }
                } else {
                        val syncStatus = venta?.syncStatus ?: "RECIBIDA"
                        val isSyncing = syncStatus == "SYNCING"
                        val esAbierta = venta.estatus.equals("Abierta", ignoreCase = true)

                        Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 16.dp, 
                                bottom = if (isTablet) 120.dp else 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isSyncing) {
                                item {
                                    VistaVerdeBanner(
                                        text = "Sincronizando cambios con el servidor...",
                                        isError = false
                                    )
                                }
                            }

                            item {
                                DetalleHeaderCard(venta = venta)
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = if (isTablet) 8.dp else 0.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VistaVerdeSectionHeader(
                                        text = "PRODUCTOS (${venta.productos.size})",
                                        modifier = Modifier.weight(1f)
                                    )

                                }
                            }

                            items(venta.productos) { producto ->
                                ProductoDetalleCard(
                                    producto = producto,
                                    onTransferClick = if (isSyncing) null else onTransferirProducto
                                )
                            }
                        }

                        // Barra Inferior Fija para el Total
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 16.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(if (isTablet) 32.dp else 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                if (esAbierta && !isSyncing) {
                                    Button(
                                        onClick = onAgregarProductos,
                                        modifier = Modifier.height(if (isTablet) 52.dp else 44.dp),
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(if (isTablet) 22.dp else 18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "AGREGAR",
                                            fontWeight = FontWeight.Black,
                                            fontSize = if (isTablet) 15.sp else 13.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", venta.total)}",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Black,
                                    style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (data.visuals.actionLabel != null) {
                        TextButton(
                            onClick = { data.performAction() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = data.visuals.actionLabel?.uppercase() ?: "",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleHeaderCard(venta: VentaDto) {
    val esAbierta = venta.estatus.equals("Abierta", ignoreCase = true)
    VistaVerdeBaseCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (venta.syncStatus != "RECIBIDA" && venta.folio == 0) "Folio: PENDIENTE" else "Folio: ${venta.folio}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                VistaVerdeStatusBadge(
                    text = if (esAbierta) "Abierta" else "Cerrada",
                    containerColor = if (esAbierta) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    textColor = if (esAbierta) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            DetalleRow("Cliente", venta.nombreCliente.replace(Regex("\\s+"), " ").trim().uppercase())
            if (venta.socioId != null && venta.socioId != 0) {
                DetalleRow("ID Socio", venta.socioId.toString())
            } else {
                DetalleRow("ID Socio", "N/A")
            }
            if (venta.tipoCliente != null) {
                DetalleRow("Tipo", venta.tipoCliente)
            }
            if (venta.fecha != null) {
                DetalleRow("Fecha", venta.fecha)
            }
            DetalleRow("Hora", venta.hora)
        }
    }
}

@Composable
private fun DetalleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

