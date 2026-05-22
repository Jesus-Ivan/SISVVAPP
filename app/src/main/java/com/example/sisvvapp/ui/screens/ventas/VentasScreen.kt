package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.VistaVerdeSaleCard
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun VentasScreen(
    onMenuClick: () -> Unit,
    ventas: List<VentaDto> = emptyList(),
    onVentaClick: (VentaDto) -> Unit = {},
    onNuevaVentaClick: () -> Unit = {}
) {
    VistaVerdeScaffold(
        title = "Ventas",
        onMenuClick = onMenuClick,
        actions = {
            IconButton(onClick = onNuevaVentaClick) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Venta")
            }
            IconButton(onClick = { /* Filtro por fecha */ }) {
                Icon(Icons.Default.DateRange, contentDescription = "Filtrar por fecha")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ventas) { venta ->
                VistaVerdeSaleCard(
                    venta = venta,
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VentasScreenPreview() {
    SISVVAPPTheme {
        VentasScreen(
            onMenuClick = {}
        )
    }
}