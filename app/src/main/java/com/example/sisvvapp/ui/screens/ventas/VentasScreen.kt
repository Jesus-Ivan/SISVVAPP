package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.LocalScaleFactor
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal

@Composable
fun VentasScreen(
    onMenuClick: () -> Unit,
    ventas: List<VentaDto> = emptyList(),
    isOnline: Boolean = true,
    onVentaClick: (VentaDto) -> Unit = {},
    onNuevaVentaClick: () -> Unit = {}
) {
    VistaVerdeScaffold(
        title = stringResource(R.string.ventas_title),
        onMenuClick = onMenuClick,
        isOnline = isOnline,
        actions = {
            IconButton(onClick = { /* Lógica de búsqueda */ }) {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.ventas_search_desc))
            }
            IconButton(onClick = { /* Filtro por fecha */ }) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.ventas_filter_date_desc))
            }
        }
    ) {
        val scale = LocalScaleFactor.current
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp * scale)
            ) {
                Spacer(modifier = Modifier.height(16.dp * scale))
                VistaVerdeSectionHeader(text = stringResource(R.string.ventas_section_recent))
                Spacer(modifier = Modifier.height(8.dp * scale))

                if (ventas.isEmpty()) {
                    VistaVerdeEmptyState(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        message = stringResource(R.string.ventas_empty_state),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(12.dp * scale)
                    ) {
                        items(
                            items = ventas,
                            key = { venta -> venta.folio }
                        ) { venta ->
                            VistaVerdeSaleCard(
                                venta = venta,
                                modifier = Modifier.clickable { onVentaClick(venta) }
                            )
                        }
                    }
                }
            }

            // 3. EL BOTÓN FLOTANTE (FAB)
            FloatingActionButton(
                onClick = onNuevaVentaClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp * scale),
                containerColor = VerdePrincipal,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp * scale)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.ventas_new_sale_desc),
                    modifier = Modifier.size(28.dp * scale)
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
