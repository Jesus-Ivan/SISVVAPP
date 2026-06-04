package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.viewmodel.CarritoItem
import com.example.sisvvapp.ui.viewmodel.SendResult
import java.util.Locale

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
    onConfirmar: () -> Unit,
    onVolver: () -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
    VistaVerdeScaffold(
        title = stringResource(R.string.resumen_title),
        subtitle = stringResource(R.string.resumen_subtitle),
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = isOnline
    ) {
        ResponsiveContainer {
            when {
                sendResult is SendResult.Success -> {
                    SuccessContent(
                        folio = sendResult.folio,
                        onVolver = onVolver
                    )
                }
                sendResult is SendResult.Error -> {
                    ErrorContent(
                        message = sendResult.message,
                        onRetry = onConfirmar,
                        onVolver = onVolver
                    )
                }
                sendResult is SendResult.Offline -> {
                    OfflineContent(onVolver = onVolver)
                }
                else -> {
                    if (items.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            VistaVerdeEmptyState(
                                icon = Icons.Default.ShoppingCart,
                                message = "No hay productos en el carrito"
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            VistaVerdeButton(
                                text = "Agregar productos",
                                onClick = onBackClick
                            )
                        }
                    } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        VistaVerdeSectionHeader(text = stringResource(R.string.resumen_title))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Datos de la venta
                        ResumenVentaRow(
                            label = "Tipo",
                            value = tipoVenta
                        )
                        ResumenVentaRow(
                            label = "Cliente",
                            value = nombreCliente
                        )
                        ResumenVentaRow(
                            label = "Caja",
                            value = "$corteCaja"
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Lista de productos
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(items) { index, item ->
                                CarritoItemCard(
                                    nombre = item.producto.descripcion,
                                    cantidad = item.cantidad,
                                    precioUnitario = item.precioUnitario,
                                    subtotal = item.subtotal,
                                    modificadores = item.modificadores.map { it.nombre },
                                    onCantidadChange = { cant -> onUpdateCantidad(index, cant) },
                                    onRemove = { onRemoveItem(index) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.resumen_total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", total)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        VistaVerdeButton(
                            text = if (isSending) stringResource(R.string.resumen_btn_enviando)
                            else stringResource(R.string.resumen_btn_confirmar),
                            onClick = onConfirmar,
                            enabled = !isSending && items.isNotEmpty()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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

@Preview(showBackground = true)
@Composable
fun ResumenCarritoScreenPreview() {
    SISVVAPPTheme {
        ResumenCarritoScreen(
            items = emptyList(),
            tipoVenta = "Socio",
            nombreCliente = "Cristian Meza",
            corteCaja = 6858,
            total = 370.0,
            isSending = false,
            sendResult = null,
            onUpdateCantidad = { _, _ -> },
            onRemoveItem = {},
            onConfirmar = {},
            onVolver = {},
            onBackClick = {}
        )
    }
}
