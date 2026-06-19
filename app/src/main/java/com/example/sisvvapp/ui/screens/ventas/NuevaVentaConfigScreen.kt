package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import com.example.sisvvapp.ui.utils.normalizeName

@Composable
fun NuevaVentaConfigScreen(
    tiposDeVenta: List<String>,
    tipoSeleccionado: String,
    onTipoVentaChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sociosEncontrados: List<SocioEntity>,
    onSocioSeleccionado: (SocioEntity?) -> Unit,
    socioSeleccionado: SocioEntity? = null,
    nombreCliente: String,
    onNombreClienteChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onContinuarClick: () -> Unit,
    isOnline: Boolean = true,
    cajasDisponibles: Boolean = true,
    isFormValid: Boolean = true,
    numeroComensales: String = "",
    onNumeroComensalesChange: (String) -> Unit = {}
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    val requiereSocio = tipoSeleccionado == "socio" || tipoSeleccionado == "invitado"
    val focusManager = LocalFocusManager.current

    val getDisplayType = { type: String ->
        when (type) {
            "socio" -> "Socio"
            "invitado" -> "Invitado del Socio"
            "general" -> "Público General"
            "empleado" -> "Empleado"
            else -> type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        }
    }

    val displayOptions = tiposDeVenta.map { getDisplayType(it) }
    val displaySelected = getDisplayType(tipoSeleccionado)

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

                VistaVerdeTextField(
                    value = numeroComensales,
                    onValueChange = onNumeroComensalesChange,
                    label = "Número de Comensales",
                    keyboardType = KeyboardType.Number,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    bgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                VistaVerdeDropdown(
                    label = "Tipo de Venta",
                    options = displayOptions,
                    selectedOption = displaySelected,
                    onOptionSelected = { nuevoDisplay ->
                        val index = displayOptions.indexOf(nuevoDisplay)
                        val realType = if (index >= 0) tiposDeVenta[index] else "general"
                        onTipoVentaChange(realType)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (requiereSocio) {
                    Text(
                        text = "ID/ Nombre Socio",
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VistaVerdeSearchBar(
                        value = searchQuery,
                        onValueChange = { nuevaBusqueda ->
                            onSearchQueryChange(nuevaBusqueda)
                            if (nuevaBusqueda.isBlank()) {
                                onSocioSeleccionado(null)
                            }
                        },
                        placeholder = "Buscar Socio"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                                    isTablet = isTablet,
                                    onClick = {
                                        onSocioSeleccionado(socio)
                                        if (socio.estatus == "CAN") {
                                            onSearchQueryChange("")
                                        } else {
                                            onSearchQueryChange("${socio.nombre} ${socio.apellidoP}".trim())
                                        }
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (tipoSeleccionado == "invitado") {
                        val nombreSocioText = if (socioSeleccionado != null) {
                            "${socioSeleccionado.nombre} ${socioSeleccionado.apellidoP} ${socioSeleccionado.apellidoM ?: ""}".trim()
                        } else {
                            ""
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        VistaVerdeTextField(
                            value = nombreSocioText,
                            onValueChange = { onNombreClienteChange(it.normalizeName()) },
                            label = "Socio",
                            readOnly = true,
                            enabled = false
                        )
                    }

                    if (socioSeleccionado?.estatus == "CAN") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Membresia de socio ${socioSeleccionado.id} cancelada",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = if (isTablet) 16.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                val labelText = when (tipoSeleccionado) {
                    "socio" -> "Socio Seleccionado"
                    "invitado" -> "Nombre del Invitado"
                    else -> "Nombre del Cliente"
                }
                val isEditable = tipoSeleccionado != "socio"

                VistaVerdeTextField(
                    value = nombreCliente,
                    onValueChange = { onNombreClienteChange(it.uppercase()) },
                    label = labelText,
                    readOnly = !isEditable,
                    enabled = isEditable,
                    capitalization = KeyboardCapitalization.Characters
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!cajasDisponibles) {
                    Text(
                        text = "No hay cajas abiertas. Ve a Ajustes y selecciona una caja.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = if (isTablet) 15.sp else 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                val botonHabilitado = cajasDisponibles && isFormValid

                VistaVerdeButton(
                    text = "Continuar \u2192",
                    onClick = onContinuarClick,
                    enabled = botonHabilitado
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun VistaVerdeSocioResultItem(
    socio: SocioEntity,
    isTablet: Boolean = false,
    onClick: () -> Unit
) {
    VistaVerdeBaseCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(if (isTablet) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}".trim(),
                    fontWeight = FontWeight.Medium,
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${socio.id}",
                    fontSize = if (isTablet) 14.sp else 12.sp,
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
            tipoSeleccionado = "Invitado del Socio",
            onTipoVentaChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            sociosEncontrados = emptyList(),
            onSocioSeleccionado = {},
            socioSeleccionado = SocioEntity(1, "Juan", "Perez", "Sanchez", null, null, true, "Activo", "", null, "Gold"),
            nombreCliente = "",
            onNombreClienteChange = {},
            onMenuClick = {},
            onContinuarClick = {},
            isOnline = true
        )
    }
}