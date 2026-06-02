package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.TipoPagoEntity
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.viewmodel.PagoItem
import java.util.Locale

@Composable
fun SeleccionarPagoScreen(
    tiposPago: List<TipoPagoEntity>,
    pagos: List<PagoItem>,
    totalVenta: Double,
    totalPagos: Double,
    onAgregarPago: (TipoPagoEntity, Double, Double) -> Unit,
    onEliminarPago: (Int) -> Unit,
    onConfirmar: () -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
    var tipoSeleccionado by remember { mutableStateOf<TipoPagoEntity?>(null) }
    var montoTexto by remember { mutableStateOf("") }
    var propinaTexto by remember { mutableStateOf("") }
    var showTipoDialog by remember { mutableStateOf(false) }

    val restante = totalVenta - totalPagos
    val isValid = restante <= 0.01 && pagos.isNotEmpty()

    VistaVerdeScaffold(
        title = "Pagos",
        subtitle = "Total: $${String.format(Locale.US, "%.2f", totalVenta)}",
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = isOnline
    ) {
        ResponsiveContainer {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                VistaVerdeSectionHeader(text = "Agregar Pago")
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de tipo de pago
                OutlinedButton(
                    onClick = { showTipoDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tipoSeleccionado?.nombre ?: "Seleccionar tipo de pago",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de monto
                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de propina
                OutlinedTextField(
                    value = propinaTexto,
                    onValueChange = { propinaTexto = it },
                    label = { Text("Propina (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val monto = montoTexto.toDoubleOrNull() ?: 0.0
                        val propina = propinaTexto.toDoubleOrNull() ?: 0.0
                        if (tipoSeleccionado != null && monto > 0) {
                            onAgregarPago(tipoSeleccionado!!, monto, propina)
                            montoTexto = ""
                            propinaTexto = ""
                            tipoSeleccionado = null
                        }
                    },
                    enabled = tipoSeleccionado != null && (montoTexto.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar Pago")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$${String.format(Locale.US, "%.2f", totalVenta)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pagado:", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format(Locale.US, "%.2f", totalPagos)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Restante:", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = if (restante > 0.01) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Text("$${String.format(Locale.US, "%.2f", restante)}", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = if (restante > 0.01) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de pagos
                if (pagos.isNotEmpty()) {
                    VistaVerdeSectionHeader(text = "Pagos Registrados")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pagos) { pago ->
                            PagoItemCard(
                                pago = pago,
                                onEliminar = { onEliminarPago(pagos.indexOf(pago)) }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                VistaVerdeButton(
                    text = stringResource(R.string.resumen_btn_confirmar),
                    onClick = onConfirmar,
                    enabled = isValid
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Diálogo de selección de tipo de pago
    if (showTipoDialog) {
        AlertDialog(
            onDismissRequest = { showTipoDialog = false },
            title = { Text("Tipo de Pago") },
            text = {
                LazyColumn {
                    items(tiposPago) { tipo ->
                        ListItem(
                            headlineContent = { Text(tipo.nombre) },
                            modifier = Modifier.clickable {
                                tipoSeleccionado = tipo
                                showTipoDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTipoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PagoItemCard(pago: PagoItem, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pago.tipoPago.nombre, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("$${String.format(Locale.US, "%.2f", pago.monto)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (pago.propina > 0) {
                    Text("Propina: $${String.format(Locale.US, "%.2f", pago.propina)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeleccionarPagoScreenPreview() {
    SISVVAPPTheme {
        SeleccionarPagoScreen(
            tiposPago = emptyList(),
            pagos = emptyList(),
            totalVenta = 370.0,
            totalPagos = 0.0,
            onAgregarPago = { _, _, _ -> },
            onEliminarPago = {},
            onConfirmar = {},
            onBackClick = {}
        )
    }
}
