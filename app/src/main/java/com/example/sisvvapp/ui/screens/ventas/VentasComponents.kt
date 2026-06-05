package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.network.dto.ventas.ProductoVentaDto
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeStatusBadge
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import java.util.Locale

@Composable
fun ProductoDetalleCard(
    producto: ProductoVentaDto,
    modifier: Modifier = Modifier,
    onTransferClick: ((ProductoVentaDto) -> Unit)? = null
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET

    VistaVerdeBaseCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Cantidad con un estilo de Badge circular
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(if (isTablet) 48.dp else 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${producto.cantidad}",
                        style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (isTablet) 20.dp else 16.dp))

            // 2. Información central (Nombre y Notas)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (producto.observaciones.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = producto.observaciones,
                            style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Botón de transferencia (si aplica)
            if (onTransferClick != null) {
                IconButton(
                    onClick = { onTransferClick(producto) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Transferir",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }
            }

            // 4. Precio con tipografía destacada
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(Locale.US, "%.2f", producto.subtotal)}",
                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Unit: $${String.format(Locale.US, "%.2f", producto.precio)}",
                    style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VistaVerdeSaleCard(
    venta: VentaDto,
    modifier: Modifier = Modifier
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET
    val esAbierta = venta.estatus.equals("Abierta", ignoreCase = true)

    VistaVerdeBaseCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Folio: ${venta.folio}",
                    fontSize = if (isTablet) 13.sp else 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 6.dp))
                Text(
                    text = "${venta.socioId ?: "N/A"} - ${venta.nombreCliente}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isTablet) 18.sp else 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 6.dp))
                Text(
                    text = "${venta.fecha ?: "Sin fecha"} | ${venta.hora}",
                    fontSize = if (isTablet) 13.sp else 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                VistaVerdeStatusBadge(
                    text = if (esAbierta) "Abierta" else "Cerrada",
                    containerColor = if (esAbierta) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    textColor = if (esAbierta) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
                Text(
                    text = "TOTAL: $${String.format(Locale.US, "%.2f", venta.total)}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 18.sp else 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun VentasList(
    ventas: List<VentaDto>,
    onVentaClick: (VentaDto) -> Unit
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET

    LazyVerticalGrid(
        columns = GridCells.Fixed(if (isTablet) 2 else 1),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = ventas, key = { venta -> venta.folio }) { venta ->
            VistaVerdeSaleCard(
                venta = venta,
                modifier = Modifier.clickable { onVentaClick(venta) }
            )
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun SaleCardPreview() {
    SISVVAPPTheme {
        val mockVenta = VentaDto(
            folio = 59490, nombreCliente = "Cristian Meza", hora = "15:34",
            total = 983.0, estatus = "Abierta", cajaId = 1, socioId = 1832,
            tipoCliente = "Socio", fecha = "15/06/2026", productos = emptyList(), pagos = emptyList()
        )
        Surface(modifier = Modifier.padding(16.dp)) {
            VistaVerdeSaleCard(venta = mockVenta)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VentasListPreview() {
    SISVVAPPTheme {
        val mockVentas = listOf(
            VentaDto(1, "Cristian Meza", "15:34", 983.0, "Abierta", 1, 1832, "Socio", "15/06/2026"),
            VentaDto(2, "Juan Pérez", "16:00", 500.0, "Cerrada", 1, 1833, "Socio", "15/06/2026")
        )
        Surface(modifier = Modifier.fillMaxSize()) {
            VentasList(ventas = mockVentas, onVentaClick = {})
        }
    }
}