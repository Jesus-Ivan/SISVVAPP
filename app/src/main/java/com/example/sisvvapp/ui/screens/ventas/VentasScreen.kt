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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasScreen(
    onMenuClick: () -> Unit,
    ventas: List<VentaDto> = emptyList(),
    isOnline: Boolean = true,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onVentaClick: (VentaDto) -> Unit = {},
    onNuevaVentaClick: () -> Unit = {}
) {
    VistaVerdeScaffold(
        title = stringResource(R.string.ventas_title),
        subtitle = stringResource(R.string.ventas_subtitle),
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
        ResponsiveContainer {
            Box(modifier = Modifier.fillMaxSize()) {

                val pullState = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = onRefresh,
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullState,
                            isRefreshing = isLoading,
                            containerColor = MaterialTheme.colorScheme.surface,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        VistaVerdeSectionHeader(text = stringResource(R.string.ventas_section_recent))
                        Spacer(modifier = Modifier.height(8.dp))

                        if (ventas.isEmpty() && !isLoading) {
                            VistaVerdeEmptyState(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                message = stringResource(R.string.ventas_empty_state),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                }

                // FAB
                FloatingActionButton(
                    onClick = onNuevaVentaClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.ventas_new_sale_desc),
                        modifier = Modifier.size(28.dp)
                    )
                }
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
