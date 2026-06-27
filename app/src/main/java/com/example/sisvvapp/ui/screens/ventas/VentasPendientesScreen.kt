package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.Poppins
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

@Composable
fun VentasPendientesScreen(
    ventas: List<VentaColaEntity>,
    onBackClick: () -> Unit,
    onDescartar: (String) -> Unit,
    onPausarSync: () -> Unit,
    onReanudarSync: () -> Unit
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    var ventaADescartar by remember { mutableStateOf<VentaColaEntity?>(null) }
    var ventaADetallar by remember { mutableStateOf<VentaColaEntity?>(null) }

    // Control de ciclo de vida del Sync propuesto en el plan
    DisposableEffect(Unit) {
        onPausarSync()
        onDispose {
            onReanudarSync()
        }
    }

    VistaVerdeScaffold(
        title = "Cola de Sincronización",
        subtitle = "${ventas.size} pedidos pendientes",
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = false // Siempre mostramos offline en esta pantalla por contexto
    ) {
        ResponsiveContainer {
            if (ventas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    VistaVerdeEmptyState(
                        icon = Icons.Default.CloudOff,
                        message = "No hay ventas pendientes de envío"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        VistaVerdeBanner(
                            text = "Estas ventas se enviarán automáticamente cuando salgas de esta pantalla y recuperes conexión.",
                            isError = false
                        )
                    }
                    
                    items(ventas, key = { it.idTemporal }) { venta ->
                        VentaPendienteCard(
                            venta = venta,
                            isTablet = isTablet,
                            onDescartarClick = { ventaADescartar = venta },
                            onVerDetalle = { ventaADetallar = venta }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Detalle de Productos
    ventaADetallar?.let { venta ->
        val productos: List<ItemCarritoDto> = try {
            val type = object : TypeToken<List<ItemCarritoDto>>() {}.type
            Gson().fromJson(venta.productosJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        DetalleVentaPendienteDialog(
            venta = venta,
            productos = productos,
            onDismiss = { ventaADetallar = null }
        )
    }

    // Diálogo de Confirmación con Identidad Visual
    ventaADescartar?.let { venta ->
        DescartarVentaDialog(
            montoTotal = venta.totalVenta,
            esEdicion = (venta.folioExistente ?: 0) > 0,
            onConfirm = { 
                onDescartar(venta.idTemporal)
                ventaADescartar = null
            },
            onDismiss = { ventaADescartar = null }
        )
    }
}

@Composable
private fun VentaPendienteCard(
    venta: VentaColaEntity,
    isTablet: Boolean,
    onDescartarClick: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val esEdicion = (venta.folioExistente ?: 0) > 0
    val isSyncing = venta.estado == "SYNCING"

    VistaVerdeBaseCard(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onVerDetalle() },
        border = if (isSyncing) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VistaVerdeStatusBadge(
                            text = if (esEdicion) "EDICIÓN LOCAL" else "NUEVA VENTA LOCAL",
                            containerColor = if (esEdicion) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            textColor = if (esEdicion) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (isSyncing) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = venta.nombreCliente.uppercase(),
                            style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp).padding(start = 4.dp)
                        )
                    }
                    if (esEdicion) {
                        Text(
                            text = "Folio Original: ${venta.folioExistente}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", venta.totalVenta)}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isTablet) 20.sp else 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDescartarClick,
                        enabled = !isSyncing,
                        modifier = Modifier.size(32.dp).padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Descartar",
                            tint = if (isSyncing) Color.Gray else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetalleVentaPendienteDialog(
    venta: VentaColaEntity,
    productos: List<ItemCarritoDto>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        VistaVerdeBaseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETALLE DEL PEDIDO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = venta.nombreCliente.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(productos) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${item.cantidad}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.nombre ?: "Producto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (!item.observaciones.isNullOrBlank()) {
                                    Text(
                                        text = item.observaciones,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("CERRAR", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun DescartarVentaDialog(
    montoTotal: Double,
    esEdicion: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        VistaVerdeBaseCard(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (esEdicion) "¿Descartar edición?" else "¿Descartar venta nueva?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Esta acción es irreversible. Se perderán los productos por un valor de:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${String.format(Locale.US, "%.2f", montoTotal)}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botones Apilados Verticalmente (Feedback del Usuario)
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("SÍ, DESCARTAR", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
