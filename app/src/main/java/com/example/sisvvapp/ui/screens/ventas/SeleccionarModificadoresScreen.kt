package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.sisvvapp.ui.components.VistaVerdeTextField
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun SeleccionarModificadoresScreen(
    producto: ProductoEntity,
    gruposModificadores: List<GrupoModificadorEntity>,
    modificadoresDisponibles: List<ModificadorEntity>,
    cantidadProducto: Int = 1,
    onAddToCart: (List<ModificadorEntity>, String) -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
    val selectedModificadores = remember { mutableStateListOf<ModificadorEntity>() }
    var observaciones by remember { mutableStateOf("") }
    
    // Estado para el filtro de grupo seleccionado
    var selectedGroupId by remember(gruposModificadores) { 
        mutableStateOf(gruposModificadores.firstOrNull()?.idGrupo) 
    }

    val isTablet = com.example.sisvvapp.ui.utils.LocalDeviceType.current == com.example.sisvvapp.ui.utils.DeviceType.TABLET

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
                // Producto Principal con estilo resaltado
                Text(
                    text = producto.descripcion.uppercase(),
                    style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                HorizontalDivider(
                    thickness = if (isTablet) 4.dp else 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Selector de Grupos (Filtro tipo Chips/Botones)
                if (gruposModificadores.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(gruposModificadores) { grupo ->
                            val isSelected = selectedGroupId == grupo.idGrupo
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGroupId = grupo.idGrupo },
                                label = {
                                    Text(
                                        text = grupo.descripcionGrupo,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = MaterialTheme.shapes.medium,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (gruposModificadores.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.modificadores_sin_opciones),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val grupoAMostrar = gruposModificadores.find { it.idGrupo == selectedGroupId }
                        
                        if (grupoAMostrar != null) {
                            item(key = "${grupoAMostrar.claveProducto}_${grupoAMostrar.idGrupo}") {
                                val modificadoresDelGrupo = modificadoresDisponibles
                                    .filter { it.grupo == grupoAMostrar.idGrupo.toString() }

                                val seleccionadosDelGrupo = selectedModificadores
                                    .filter { it.grupo == grupoAMostrar.idGrupo.toString() }

                                GrupoModificadoresSection(
                                    grupo = grupoAMostrar,
                                    modificadores = modificadoresDelGrupo,
                                    seleccionados = seleccionadosDelGrupo,
                                    cantidadProducto = cantidadProducto,
                                    onAdd = { mod ->
                                        selectedModificadores.add(mod)
                                    },
                                    onRemove = { mod ->
                                        // Remove just one instance of the modifier
                                        val index = selectedModificadores.indexOfFirst { it.id == mod.id }
                                        if (index != -1) {
                                            selectedModificadores.removeAt(index)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        VistaVerdeTextField(
                            value = observaciones,
                            onValueChange = { observaciones = it },
                            label = "Observaciones / Notas especiales",
                            placeholder = "Ej. Sin cebolla, bien cocido...",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VistaVerdeButton(
                    text = stringResource(R.string.modificadores_agregar),
                    onClick = { onAddToCart(selectedModificadores.toList(), observaciones) }
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
    cantidadProducto: Int,
    onAdd: (ModificadorEntity) -> Unit,
    onRemove: (ModificadorEntity) -> Unit
) {
    val maxPermitidos = grupo.modifMaximos * cantidadProducto
    val incluidos = grupo.modifIncluidos * cantidadProducto
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
            val count = seleccionados.count { it.id == mod.id }
            val canAdd = !enLimite

            // Determine if each specific instance in selected list is included
            // An item is included if its overall index in the selected list for this group is less than the group's limit of included modifiers.
            // When we render a specific modifier, we can determine how many of them are included.
            // Let's count how many instances of this mod are included.
            var includedCount = 0
            seleccionados.forEachIndexed { idx, selectedMod ->
                if (selectedMod.id == mod.id && idx < incluidos) {
                    includedCount++
                }
            }

            ModificadorQuantityItem(
                modificador = mod,
                count = count,
                canAdd = canAdd,
                includedCount = includedCount,
                grupoIncluidos = incluidos,
                seleccionadosTotal = seleccionados.size,
                onAdd = { onAdd(mod) },
                onRemove = { onRemove(mod) }
            )
        }
    }
}

@Composable
private fun ModificadorQuantityItem(
    modificador: ModificadorEntity,
    count: Int,
    canAdd: Boolean,
    includedCount: Int,
    grupoIncluidos: Int,
    seleccionadosTotal: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val isSelected = count > 0
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modificador.nombre,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (modificador.precio > 0) {
                    val nonIncludedCount = count - includedCount
                    val textLabel = if (count == 0) {
                        // If we haven't selected it yet, let's see if there is still space in the "incluidos" quota
                        val hasFreeSlotsRemaining = seleccionadosTotal < grupoIncluidos
                        if (hasFreeSlotsRemaining) "Incluido" else "+$${String.format(java.util.Locale.US, "%.2f", modificador.precio)}"
                    } else {
                        val parts = mutableListOf<String>()
                        if (includedCount > 0) parts.add("$includedCount Incluido(s)")
                        if (nonIncludedCount > 0) parts.add("$nonIncludedCount de pago (+$${String.format(java.util.Locale.US, "%.2f", modificador.precio * nonIncludedCount)})")
                        parts.joinToString(", ")
                    }
                    Text(
                        text = textLabel,
                        fontSize = 12.sp,
                        color = if (includedCount > 0 || (count == 0 && seleccionadosTotal < grupoIncluidos)) MaterialTheme.colorScheme.primary
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                    .padding(horizontal = 2.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    enabled = count > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Remove,
                        contentDescription = "Restar",
                        tint = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = onAdd,
                    enabled = canAdd,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Sumar",
                        tint = if (canAdd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
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
            cantidadProducto = 1,
            onAddToCart = { _, _ -> },
            onBackClick = {}
        )
    }
}
