package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeStatusBadge
import java.util.Locale

@Composable
fun VistaVerdeSaleCard(
    venta: VentaDto,
    modifier: Modifier = Modifier
) {
    val esAbierta = venta.estatus.equals("Abierta", ignoreCase = true)

    VistaVerdeBaseCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Folio: ${venta.folio}", fontSize = 11.sp, color = TextoSecundarioClaro)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${venta.socioId ?: "N/A"} - ${venta.nombreCliente}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextoPrincipalClaro
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${venta.fecha ?: "Sin fecha"} | ${venta.hora}",
                    fontSize = 11.sp,
                    color = TextoSecundarioClaro
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                VistaVerdeStatusBadge(
                    text = if (esAbierta) "Abierta" else "Cerrada",
                    containerColor = if (esAbierta) FondoAbierta else EstadoExitoClaro,
                    textColor = if (esAbierta) TextoAbierta else VerdePrincipal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    // Solución: Usamos Locale.US para asegurar el formato consistente
                    text = "TOTAL: $${String.format(Locale.US, "%.2f", venta.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = VerdePrincipal
                )
            }
        }
    }
}

@Composable
fun VentasList(ventas: List<VentaDto>, onVentaClick: (VentaDto) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = ventas, key = {venta -> venta.folio}) { venta ->
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
            folio = 59490,
            nombreCliente = "Cristian Meza",
            hora = "15:34",
            total = 983.0,
            estatus = "Abierta",
            cajaId = 1,
            socioId = 1832,
            tipoCliente = "Socio",
            fecha = "15/06/2026",
            productos = emptyList(),
            pagos = emptyList()
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
