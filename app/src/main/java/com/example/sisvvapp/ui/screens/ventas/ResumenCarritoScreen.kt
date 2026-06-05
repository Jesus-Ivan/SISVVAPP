package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
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
import com.example.sisvvapp.ui.viewmodel.CarritoItem
import com.example.sisvvapp.ui.viewmodel.SendResult
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenCarritoScreen(
    items: List<CarritoItem>,
    tipoVenta: String,
    nombreCliente: String,
    corteCaja: Int,
    total: Double,
    isSending: Boolean,
    sendResult: SendResult?,
    onUpdateCantidad: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
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
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
                            VistaVerdeSectionHeader(text = stringResource(R.string.resumen_title))
                            Spacer(modifier = Modifier.height(16.dp))

                            ResumenVentaRow("Tipo", tipoVenta)
                            ResumenVentaRow("Cliente", nombreCliente)
                            ResumenVentaRow("Caja", "$corteCaja")

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(items = items, key = { index, item -> "${item.producto.id}-$index" }) { index, item ->

                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { dismissValue ->
                                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onRemoveItem(index)
                                                mostrarNotificacion(item, index)
                                                true
                                            } else false
                                        },
                                        // Sensibilidad aumentada a 0.5f para evitar borrados accidentales
                                        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
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
                                                onCantidadChange = { cant -> onUpdateCantidad(index, cant) }
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("$${String.format(Locale.US, "%.2f", total)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            VistaVerdeButton(text = if (isSending) "Enviando..." else "Confirmar Venta", onClick = onConfirmar, enabled = !isSending)
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)) { data ->
            Snackbar(snackbarData = data, shape = MaterialTheme.shapes.medium)
        }
    }
}
@Composable
private fun SuccessContent(folio: Int, onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.resumen_venta_exitosa),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Folio: $folio",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        VistaVerdeButton(
            text = stringResource(R.string.resumen_volver),
            onClick = onVolver
        )
    }

}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.resumen_venta_error),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        VistaVerdeButton(
            text = "Reintentar",
            onClick = onRetry
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onVolver) {
            Text(text = stringResource(R.string.resumen_volver))
        }
    }
}

@Composable
private fun OfflineContent(onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.resumen_venta_offline),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "La venta se enviará automáticamente cuando se restaure la conexión.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        VistaVerdeButton(
            text = stringResource(R.string.resumen_volver),
            onClick = onVolver
        )
    }
}


