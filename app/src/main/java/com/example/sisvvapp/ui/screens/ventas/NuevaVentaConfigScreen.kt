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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeDropdown
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.components.VistaVerdeTextField
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun NuevaVentaConfigScreen(
    tiposDeVenta: List<String>,
    tipoSeleccionado: String,
    onTipoVentaChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sociosEncontrados: List<SocioEntity>,
    onSocioSeleccionado: (SocioEntity) -> Unit,
    nombreCliente: String,
    onNombreClienteChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onContinuarClick: () -> Unit,
    isOnline: Boolean = true,
    isFormValid: Boolean = true,
    cajasDisponibles: Boolean = true
) {
    val mostrarBuscador = tipoSeleccionado == "Socio" || tipoSeleccionado == "Invitado del Socio"
    val nombreEditable = tipoSeleccionado != "Socio"

    VistaVerdeScaffold(
        title = "Nueva Venta",
        onMenuClick = onMenuClick,
        isBackButton = true,
        isOnline = isOnline
    ) {
        ResponsiveContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                VistaVerdeSectionHeader(text = "Seleccione tipo de venta")
                Spacer(modifier = Modifier.height(24.dp))

                VistaVerdeDropdown(
                    label = "Tipo de Venta",
                    options = tiposDeVenta,
                    selectedOption = tipoSeleccionado,
                    onOptionSelected = onTipoVentaChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (mostrarBuscador) {
                    Text(
                        text = "ID/ Nombre Socio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VistaVerdeSearchBar(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = "Buscar Socio"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de resultados de búsqueda
                    if (sociosEncontrados.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = sociosEncontrados,
                                key = { it.id }
                            ) { socio ->
                                VistaVerdeSocioResultItem(
                                    socio = socio,
                                    onClick = { onSocioSeleccionado(socio) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                VistaVerdeTextField(
                    value = nombreCliente,
                    onValueChange = onNombreClienteChange,
                    label = if (tipoSeleccionado == "Socio") "Socio Encontrado" else "Nombre",
                    readOnly = !nombreEditable,
                    enabled = nombreEditable
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!cajasDisponibles) {
                    Text(
                        text = "No hay cajas abiertas. Ve a Ajustes y selecciona una caja.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                VistaVerdeButton(
                    text = "Continuar \u2192",
                    onClick = onContinuarClick,
                    enabled = isFormValid && cajasDisponibles
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun VistaVerdeSocioResultItem(
    socio: SocioEntity,
    onClick: () -> Unit
) {
    VistaVerdeBaseCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${socio.id}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NuevaVentaConfigScreenPreview() {
    SISVVAPPTheme {
        NuevaVentaConfigScreen(
            tiposDeVenta = listOf("Público General", "Socio", "Invitado del Socio", "Empleado"),
            tipoSeleccionado = "Socio",
            onTipoVentaChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            sociosEncontrados = emptyList(),
            onSocioSeleccionado = {},
            nombreCliente = "",
            onNombreClienteChange = {},
            onMenuClick = {},
            onContinuarClick = {},
            isOnline = true
        )
    }
}
