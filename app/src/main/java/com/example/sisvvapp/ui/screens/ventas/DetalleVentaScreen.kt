package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
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
    onTransferirProducto: ((ProductoVentaDto) -> Unit)? = null
) {
    VistaVerdeScaffold(
        title = "Detalle de Venta",
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = isOnline
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
                val esAbierta = venta.estatus.equals("Abierta", ignoreCase = true)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DetalleHeaderCard(venta = venta)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VistaVerdeSectionHeader(
                                text = "PRODUCTOS (${venta.productos.size})",
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (esAbierta) {
                                Button(
                                    onClick = onAgregarProductos,
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("AGREGAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    items(venta.productos) { producto ->
                        ProductoDetalleCard(
                            producto = producto,
                            onTransferClick = onTransferirProducto
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        VistaVerdeBaseCard {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Monto Total", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", venta.total)}",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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

