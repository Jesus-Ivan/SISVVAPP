package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import androidx.compose.material3.*
import com.example.sisvvapp.network.dto.cajas.CajaDto
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import com.example.sisvvapp.ui.viewmodel.VentasUiState

@Composable
fun VentasScreen(
    onMenuClick: () -> Unit,
    uiState: VentasUiState,
    isOnline: Boolean = true,
    searchQuery: String = "",
    selectedDate: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    nombreCaja: String,
    onVentaClick: (VentaDto) -> Unit = {},
    onNuevaVentaClick: () -> Unit = {},
    onVentasPendientesClick: () -> Unit = {},
    onDateSelected: (String) -> Unit = {},
    onClearDate: () -> Unit = {}
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val isTablet = LocalDeviceType.current == DeviceType.TABLET

    // Conteo de ventas pendientes para el badge
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.example.sisvvapp.data.local.AppDatabase.getInstance(context) }
    val pendientesCount by db.ventaColaDao().countAllPendientesFlow().collectAsState(initial = 0)

    VistaVerdeScaffold(
        title = stringResource(R.string.ventas_title),
        onMenuClick = onMenuClick,
        subtitle = "Caja: $nombreCaja",
        isOnline = isOnline,
        actions = {
            if (pendientesCount > 0) {
                IconButton(onClick = onVentasPendientesClick) {
                    BadgedBox(
                        badge = {
                            Badge { Text("$pendientesCount") }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Ventas pendientes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.ventas_filter_date_desc))
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar ventas")
            }
        }
    ) {
        ResponsiveContainer {
            Box(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VistaVerdeSectionHeader(text = stringResource(R.string.ventas_section_recent))
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Chip indicador de fecha cuando no es hoy
                    val today = java.time.LocalDate.now().toString()
                    if (selectedDate.isNotBlank() && selectedDate != today) {
                        val displayDate = try {
                            val parts = selectedDate.split("-")
                            "${parts[2]}/${parts[1]}/${parts[0]}"
                        } catch (e: Exception) { selectedDate }
                        SuggestionChip(
                            onClick = onClearDate,
                            label = {
                                Text(
                                    text = "Viendo: $displayDate",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Volver a hoy",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    VistaVerdeSearchBar(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = stringResource(R.string.ventas_search_placeholder),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    when (uiState) {
                        is VentasUiState.Loading -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(6) {
                                    VistaVerdeSkeletonCard()
                                }
                            }
                        }
                        is VentasUiState.Empty -> {
                            VistaVerdeEmptyState(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                message = stringResource(R.string.ventas_empty_state),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        is VentasUiState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(uiState.message, color = MaterialTheme.colorScheme.error)
                                Button(onClick = onRefresh) { Text("Reintentar") }
                            }
                        }
                        is VentasUiState.NetworkError -> {
                            VentasList(
                                ventas = uiState.ventasLocales,
                                searchQuery = searchQuery,
                                onVentaClick = onVentaClick
                            )
                        }
                        is VentasUiState.Success -> {
                            VentasList(
                                ventas = uiState.ventas,
                                searchQuery = searchQuery,
                                onVentaClick = onVentaClick
                            )
                        }
                    }
                }
                
                // ... FloatingActionButton ...

                FloatingActionButton(
                    onClick = onNuevaVentaClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(if (isTablet) 80.dp else 56.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.ventas_new_sale_desc),
                        modifier = Modifier.size(if (isTablet) 40.dp else 24.dp)
                    )
                }
            }
        }
    }

    VistaVerdeDatePicker(
        showDialog = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateSelected = { fecha ->
            showDatePicker = false
            onDateSelected(fecha)
        }
    )
}

@Composable
private fun VentasList(
    ventas: List<VentaDto>,
    searchQuery: String,
    onVentaClick: (VentaDto) -> Unit
) {
    val filteredVentas = (if (searchQuery.isBlank()) {
        ventas
    } else {
        ventas.filter { venta ->
            venta.folio.toString().contains(searchQuery, ignoreCase = true) ||
                    venta.nombreCliente.contains(searchQuery, ignoreCase = true)
        }
    }).sortedByDescending { it.hora }

    if (filteredVentas.isEmpty() && searchQuery.isNotBlank()) {
        VistaVerdeEmptyState(
            icon = Icons.Default.Search,
            message = "No se encontraron ventas para \"$searchQuery\"",
            modifier = Modifier.fillMaxSize()
        )
    } else if (filteredVentas.isEmpty()) {
        VistaVerdeEmptyState(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            message = stringResource(R.string.ventas_empty_state),
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = filteredVentas,
                key = { venta -> if (venta.syncStatus == "RECIBIDA") venta.folio else (venta.idTemporal ?: venta.hashCode()) }
            ) { venta ->
                VistaVerdeSaleCard(
                    venta = venta,
                    modifier = Modifier.clickable(enabled = venta.syncStatus != "SYNCING") { onVentaClick(venta) }
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
            onMenuClick = {},
            uiState = VentasUiState.Success(emptyList()),
            nombreCaja = "Caja de Pruebas"
        )
    }
}
