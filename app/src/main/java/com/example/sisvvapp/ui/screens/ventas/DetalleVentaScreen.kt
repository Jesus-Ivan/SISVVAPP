package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.network.dto.ventas.PagoDto
import com.example.sisvvapp.network.dto.ventas.ProductoVentaDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.components.VistaVerdeStatusBadge
import com.example.sisvvapp.ui.theme.Poppins
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleVentaScreen(
    venta: VentaDto?,
    isLoading: Boolean,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onAgregarProductos: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Venta") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (venta == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                VistaVerdeEmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    message = "Venta no encontrada"
                )
            }
            return@Scaffold
        }

        val esAbierta = venta.estatus.equals("Abierta", ignoreCase = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DetalleHeaderCard(venta = venta)
            }
            item {
                VistaVerdeSectionHeader(text = "Productos (${venta.productos.size})")
            }
            items(venta.productos) { producto ->
                ProductoItemCard(producto = producto)
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                VistaVerdeSectionHeader(text = "Pagos (${venta.pagos.size})")
            }
            if (venta.pagos.isEmpty()) {
                item {
                    Text(
                        text = "Sin pagos registrados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(venta.pagos) { pago ->
                    PagoItemCard(pago = pago)
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                VistaVerdeBaseCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", venta.total)}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (esAbierta) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAgregarProductos,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isOnline
                    ) {
                        Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar productos a esta venta", fontSize = 16.sp)
                    }
                    if (!isOnline) {
                        Text(
                            text = "Se necesita conexión para agregar productos",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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
                    text = "Folio: ${venta.folio}",
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
            DetalleRow("Cliente", venta.nombreCliente)
            if (venta.socioId != null) {
                DetalleRow("ID Socio", venta.socioId.toString())
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

@Composable
private fun ProductoItemCard(producto: ProductoVentaDto) {
    VistaVerdeBaseCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (producto.observaciones.isNotBlank()) {
                    Text(
                        text = "Obs: ${producto.observaciones}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "x${producto.cantidad}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = "$${String.format(Locale.US, "%.2f", producto.subtotal)}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PagoItemCard(pago: PagoDto) {
    VistaVerdeBaseCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Payment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Tipo pago #${pago.tipoPagoId}",
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            Text(
                text = "$${String.format(Locale.US, "%.2f", pago.monto)}",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
