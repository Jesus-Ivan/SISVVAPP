package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.Poppins
import com.example.sisvvapp.ui.viewmodel.CarritoItem
import com.example.sisvvapp.ui.viewmodel.SendResult
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenCarritoScreen(
    items: List<CarritoItem>,
    tipoVenta: String,
    isAppend: Boolean = false,
    nombreCliente: String,
    corteCaja: Int,
    clavePuntoVenta: String,
    total: Double,
    isSending: Boolean,
    sendResult: SendResult?,
    onUpdateCantidad: (Int, Int) -> Unit,
    onRemoveItem: (CarritoItem) -> Unit,
    onDeshacer: (CarritoItem, Int) -> Unit,
    onConfirmar: () -> Unit,
    onVolver: () -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun mostrarNotificacion(item: CarritoItem, index: Int) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "${item.producto.descripcion} eliminado",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onDeshacer(item, index)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VistaVerdeScaffold(
            title = stringResource(R.string.resumen_title),
            subtitle = stringResource(R.string.resumen_subtitle),
            onMenuClick = onBackClick,
            isBackButton = true,
            isOnline = isOnline
        ) {
            ResponsiveContainer {
                when {
                    sendResult is SendResult.Success -> SuccessContent(sendResult.folio, onVolver)
                    sendResult is SendResult.Error -> ErrorContent(sendResult.message, onConfirmar, onVolver)
                    sendResult is SendResult.Offline -> OfflineContent(onVolver)
                    items.isEmpty() -> {
                        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            VistaVerdeEmptyState(Icons.Default.ShoppingCart, "No hay productos en el carrito")
                            Spacer(modifier = Modifier.height(24.dp))
                            VistaVerdeButton("Agregar productos", onBackClick)
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val tipoDisplay = when (tipoVenta.lowercase()) {
                                        "socio" -> "SOCIO"
                                        "invitado" -> "INVITADO"
                                        "general" -> "PUBLICO GENERAL"
                                        "empleado" -> "EMPLEADO"
                                        else -> tipoVenta.uppercase()
                                    }
                                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            ResumenVentaRow("Tipo", tipoDisplay)
                                            ResumenVentaRow("Cliente", nombreCliente.replace(Regex("\\s+"), " "))
                                            ResumenVentaRow("Punto Venta", clavePuntoVenta.ifBlank { "Caja #$corteCaja" })
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    VistaVerdeSectionHeader(text = "DETALLE DE ARTICULOS")
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
                                        val dismissState = rememberSwipeToDismissBoxState(
                                            confirmValueChange = { dismissValue ->
                                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    onRemoveItem(item)
                                                    mostrarNotificacion(item, index)
                                                    return@rememberSwipeToDismissBoxState true
                                                }
                                                return@rememberSwipeToDismissBoxState false
                                            }
                                        )

                                        SwipeToDismissBox(
                                            state = dismissState,
                                            enableDismissFromStartToEnd = false,
                                            enableDismissFromEndToStart = true,
                                            backgroundContent = {
                                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                                    MaterialTheme.colorScheme.errorContainer else Color.Transparent
                                                Box(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).background(color, MaterialTheme.shapes.medium), contentAlignment = Alignment.CenterEnd) {
                                                    Icon(Icons.Default.Delete, null, modifier = Modifier.padding(end = 24.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                                }
                                            },
                                            content = {
                                                CarritoItemCard(
                                                    nombre = item.producto.descripcion,
                                                    cantidad = item.cantidad,
                                                    subtotal = item.subtotal,
                                                    modificadores = item.modificadores.map { it.nombre },
                                                    observacion = item.observaciones,
                                                    onCantidadChange = { cant -> onUpdateCantidad(index, cant) }
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Barra Inferior Fija
                            Surface(
                                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 16.dp,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(0.5f)) {
                                        Text("TOTAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                        Text(text = "$${String.format(Locale.US, "%.2f", total)}", style = MaterialTheme.typography.titleLarge, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(onClick = onConfirmar, enabled = !isSending, modifier = Modifier.weight(0.5f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                                        if (isSending) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        else Text(if (isAppend) "ACTUALIZAR VENTA" else "REALIZAR VENTA", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp, start = 24.dp, end = 24.dp)) { data ->
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(data.visuals.message, modifier = Modifier.weight(1f))
                    TextButton(onClick = { data.performAction() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(data.visuals.actionLabel ?: "")
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(folio: Int, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.resumen_venta_exitosa), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Folio: $folio", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        VistaVerdeButton(stringResource(R.string.resumen_volver), onVolver)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Error, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.resumen_venta_error), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        VistaVerdeButton("Reintentar", onRetry)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onVolver) { Text(stringResource(R.string.resumen_volver)) }
    }
}

@Composable
private fun OfflineContent(onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.resumen_venta_offline), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("La venta se enviará automáticamente cuando se restaure la conexión.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        VistaVerdeButton(stringResource(R.string.resumen_volver), onVolver)
    }
}