package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.components.ResponsiveContainer
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

    // Variables del Buscador
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,

    // Variables del Nombre (Ahora manejado por tu VistaVerdeTextField)
    nombreCliente: String,
    onNombreClienteChange: (String) -> Unit,

    onMenuClick: () -> Unit,
    onContinuarClick: () -> Unit,
    isOnline: Boolean = true
) {
    // ---- LÓGICA DE NEGOCIO ----
    // 1. ¿Cuándo mostramos el buscador con la lupa?
    val mostrarBuscador = tipoSeleccionado == "Socio" || tipoSeleccionado == "Invitado del Socio"

    // 2. ¿Cuándo dejamos que el usuario escriba el nombre libremente?
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

                // -- SECCIÓN CONDICIONAL: BUSCADOR DE SOCIO --
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
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // -- SECCIÓN PERMANENTE: NOMBRE DEL CLIENTE --
                VistaVerdeTextField(
                    value = nombreCliente,
                    onValueChange = onNombreClienteChange,
                    label = if (tipoSeleccionado == "Socio") "Socio Encontrado" else "Nombre",
                    readOnly = !nombreEditable,
                    enabled = nombreEditable
                )

                Spacer(modifier = Modifier.weight(1f))

                VistaVerdeButton(
                    text = "Continuar \u2192",
                    onClick = onContinuarClick
                )
                Spacer(modifier = Modifier.height(16.dp))
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
            tipoSeleccionado = "Empleado",
            onTipoVentaChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            nombreCliente = "",
            onNombreClienteChange = {},
            onMenuClick = {},
            onContinuarClick = {},
            isOnline = true
        )
    }
}