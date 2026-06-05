package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.sisvvapp.ui.theme.Poppins
import com.example.sisvvapp.ui.viewmodel.CarritoItem
import com.example.sisvvapp.ui.viewmodel.SendResult
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.sisvvapp.ui.theme.Poppins
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
                        val tipoDisplay = when (tipoVenta) {
                            "socio"    -> "SOCIO"
                            "invitado" -> "INVITADO DEL SOCIO"
                            "general"  -> "PUBLICO GENERAL"
                            "empleado" -> "EMPLEADO"
                            else       -> tipoVenta
                        }
                        val cajaDisplay = clavePuntoVenta.ifBlank { "Caja #$corteCaja" }

                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp)) {

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                tonalElevation = 0.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    ResumenVentaRow(label = "Tipo", value = tipoDisplay)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ResumenVentaRow(label = "Cliente", value = nombreCliente)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ResumenVentaRow(label = "Punto de venta", value = cajaDisplay)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                        // CONTENIDO PRINCIPAL
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header del resumen
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                                VistaVerdeSectionHeader(text = stringResource(R.string.resumen_title))
                                Spacer(modifier = Modifier.height(16.dp))
                                ResumenVentaRow("Tipo", tipoVenta)
                                ResumenVentaRow("Cliente", nombreCliente)
                                ResumenVentaRow("Caja", "$corteCaja")
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                            }

                            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(
                                    items = items,
                                    key = { _, item -> item.id }
                                ) { index, item ->

                                    val currentOnRemoveItem by rememberUpdatedState(onRemoveItem)
                                    val currentItem by rememberUpdatedState(item)
                                    val currentIndex by rememberUpdatedState(index)

                                    val density = androidx.compose.ui.platform.LocalDensity.current
                                    val dismissState = remember(density, item.id) {
                                        SwipeToDismissBoxState(
                                            initialValue = SwipeToDismissBoxValue.Settled,
                                            density = density,
                                            confirmValueChange = { dismissValue: SwipeToDismissBoxValue ->
                                                dismissValue == SwipeToDismissBoxValue.EndToStart
                                            },
                                            positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f }
                                        )
                                    }

                                    val isDismissed = dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
                                    LaunchedEffect(isDismissed) {
                                        if (isDismissed) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            kotlinx.coroutines.delay(250L) // Permite que termine la animación de deslizamiento
                                            currentOnRemoveItem(currentItem)
                                            mostrarNotificacion(currentItem, currentIndex)
                                        }
                                    }
                            // Lista de productos
                            LazyColumn(
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                                observacion = item.observaciones,
                                                onCantidadChange = { cant -> onUpdateCantidad(index, cant) }
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }

        // BARRA INFERIOR
        if (items.isNotEmpty() && sendResult == null) {
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Información de Precio
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text(
                            "TOTAL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", total)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Botón
                    Button(
                        onClick = onConfirmar,
                        enabled = !isSending,
                        modifier = Modifier.weight(0.6f).height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(
                                "REALIZAR VENTA",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = data.visuals.message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Poppins
                        )
                    }
                    if (data.visuals.actionLabel != null) {
                        TextButton(
                            onClick = { data.performAction() },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = data.visuals.actionLabel!!,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = Poppins
                            )
                        }
                    }
                }
            }
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

