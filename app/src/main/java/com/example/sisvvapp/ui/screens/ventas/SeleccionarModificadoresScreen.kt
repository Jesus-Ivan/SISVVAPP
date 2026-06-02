package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.GrupoModificadorEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun SeleccionarModificadoresScreen(
    producto: ProductoEntity,
    gruposModificadores: List<GrupoModificadorEntity>,
    modificadoresDisponibles: List<ModificadorEntity>,
    onAddToCart: (List<ModificadorEntity>) -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
    val selectedModificadores = remember { mutableStateListOf<ModificadorEntity>() }

    VistaVerdeScaffold(
        title = stringResource(R.string.modificadores_title),
        subtitle = producto.descripcion,
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = isOnline
    ) {
        ResponsiveContainer {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                VistaVerdeSectionHeader(text = producto.descripcion)
                Spacer(modifier = Modifier.height(16.dp))

                if (gruposModificadores.isEmpty()) {
                    Text(
                        text = stringResource(R.string.modificadores_sin_opciones),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = gruposModificadores,
                            key = { "${it.claveProducto}_${it.idGrupo}" }
                        ) { grupo ->
                            val modificadoresDelGrupo = modificadoresDisponibles
                                .filter { it.grupo == grupo.idGrupo.toString() }

                            val seleccionadosDelGrupo = selectedModificadores
                                .filter { it.grupo == grupo.idGrupo.toString() }

                            GrupoModificadoresSection(
                                grupo = grupo,
                                modificadores = modificadoresDelGrupo,
                                seleccionados = seleccionadosDelGrupo,
                                onToggle = { mod, isSelected ->
                                    if (isSelected) {
                                        selectedModificadores.add(mod)
                                    } else {
                                        selectedModificadores.removeAll { it.id == mod.id }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VistaVerdeButton(
                    text = stringResource(R.string.modificadores_agregar),
                    onClick = { onAddToCart(selectedModificadores.toList()) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GrupoModificadoresSection(
    grupo: GrupoModificadorEntity,
    modificadores: List<ModificadorEntity>,
    seleccionados: List<ModificadorEntity>,
    onToggle: (ModificadorEntity, Boolean) -> Unit
) {
    val maxPermitidos = grupo.modifMaximos
    val incluidos = grupo.modifIncluidos
    val enLimite = seleccionados.size >= maxPermitidos

    Column {
        // Header del grupo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = grupo.descripcionGrupo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${seleccionados.size}/$maxPermitidos",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (enLimite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (incluidos > 0) {
            Text(
                text = "$incluidos incluidos en el precio",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de modificadores del grupo
        modificadores.forEach { mod ->
            val isSelected = mod in seleccionados
            val canSelect = !enLimite || isSelected

            ModificadorCheckItem(
                modificador = mod,
                isSelected = isSelected,
                isEnabled = canSelect,
                isIncluded = incluidos > 0 && seleccionados.indexOf(mod) < incluidos,
                onToggle = { onToggle(mod, isSelected) }
            )
        }
    }
}

@Composable
private fun ModificadorCheckItem(
    modificador: ModificadorEntity,
    isSelected: Boolean,
    isEnabled: Boolean,
    isIncluded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                enabled = isEnabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modificador.nombre,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (modificador.precio > 0) {
                    Text(
                        text = if (isIncluded) "Incluido" else "+$${String.format(java.util.Locale.US, "%.2f", modificador.precio)}",
                        fontSize = 12.sp,
                        color = if (isIncluded) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Incluido",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeleccionarModificadoresScreenPreview() {
    SISVVAPPTheme {
        SeleccionarModificadoresScreen(
            producto = ProductoEntity(
                id = 1, clave = "1", descripcion = "Hamburguesa",
                precio = 185.0, categoria = "Comida", imagenUrl = null,
                forzarCaptura = false, modifIncluidos = 1, modifMaximos = 3
            ),
            gruposModificadores = emptyList(),
            modificadoresDisponibles = emptyList(),
            onAddToCart = {},
            onBackClick = {}
        )
    }
}
