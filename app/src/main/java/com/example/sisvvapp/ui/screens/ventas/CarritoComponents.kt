package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import java.util.Locale

@Composable
fun CarritoItemCard(
    modifier: Modifier = Modifier,
    nombre: String,
    cantidad: Int,
    subtotal: Double,
    modificadores: List<ModificadorDisplayInfo> = emptyList(),
    observacion: String = "",
    printDefault: Boolean = true,
    tiempo: Int? = null,
    onTiempoChange: (Int?) -> Unit = {},
    onCantidadChange: (Int) -> Unit,
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET
    // Estado para controlar si el desglose está abierto
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    VistaVerdeBaseCard(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { if (modificadores.isNotEmpty()) isExpanded = !isExpanded }
    ) {
        Column {
            // Parte Superior (Siempre visible)
            Column(modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = nombre,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isTablet) 18.sp else 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (!printDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "NO GENERA COMANDA",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                        if (observacion.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.EditNote,
                                    "Nota",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    observacion,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    // Indicador sutil de expansión
                    if (modificadores.isNotEmpty()) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp).padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector de cantidad o etiqueta simple
                    if (modificadores.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                MaterialTheme.shapes.small
                            ).padding(2.dp)
                        ) {
                            IconButton(
                                onClick = { onCantidadChange(cantidad - 1) },
                                modifier = Modifier.size(28.dp)
                            ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp)) }
                            Text(
                                "$cantidad",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { onCantidadChange(cantidad + 1) },
                                modifier = Modifier.size(28.dp)
                            ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
                        }
                    } else {
                        // Si tiene modificadores, solo mostramos la cantidad como etiqueta
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "CANTIDAD: $cantidad",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        "$${String.format(Locale.US, "%.2f", subtotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 10.dp))
                if (printDefault) {
                    TiempoSelectorRow(
                        tiempo = tiempo,
                        onTiempoChange = onTiempoChange,
                        isTablet = isTablet
                    )
                }
            }

            // Desglose de Modificadores (Expandible)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "MODIFICADORES SELECCIONADOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    modificadores.forEach { mod ->
                        Column(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                            val modifierLabel = when {
                                mod.incluido -> "${mod.cantidad}x ${mod.nombre} (incluido)"
                                mod.precio > 0 -> "${mod.cantidad}x ${mod.nombre} (+$${String.format(Locale.US, "%.2f", mod.precio * mod.cantidad)})"
                                else -> "${mod.cantidad}x ${mod.nombre}"
                            }
                            Text(
                                text = "• $modifierLabel",
                                style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                                color = if (mod.incluido) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                            if (mod.nota.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                                    Icon(Icons.Default.EditNote, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = mod.nota,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}
}

@Composable
fun TiempoSelectorRow(
    tiempo: Int?,
    onTiempoChange: (Int?) -> Unit,
    isTablet: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 8.dp else 6.dp)
    ) {
        Text(
            text = "TIEMPO",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(end = if (isTablet) 12.dp else 8.dp)
        )
        (1..4).forEach { n ->
            val selected = tiempo == n
            Surface(
                shape = RoundedCornerShape(if (isTablet) 12.dp else 10.dp),
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(if (isTablet) 44.dp else 34.dp)
                    .clickable {
                        onTiempoChange(n)
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "$n",
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = if (isTablet) 16.sp else 13.sp
                    )
                }
            }
        }
    }
}

data class ModificadorDisplayInfo(
    val nombre: String,
    val cantidad: Int,
    val nota: String = "",
    val incluido: Boolean = false,
    val precio: Double = 0.0
)

@Composable
fun ResumenVentaRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = if (isTablet) 6.dp else 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isTablet) 16.sp else 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            fontSize = if (isTablet) 16.sp else 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CarritoItemCardPreview() {
    SISVVAPPTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            CarritoItemCard(
                nombre = "Hamburguesa Vista Verde",
                cantidad = 2,
                subtotal = 370.0,
                printDefault = false,
                tiempo = 2,
                modificadores = listOf(
                    ModificadorDisplayInfo("Sin cebolla", 1),
                    ModificadorDisplayInfo("Extra queso", 1, "Muy fundido")
                ),
                onCantidadChange = {},

            )
        }
    }
}
