package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    nombre: String,
    cantidad: Int,
    subtotal: Double,
    modificadores: List<String> = emptyList(),
    observacion: String = "",
    onCantidadChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET

    VistaVerdeBaseCard(modifier = modifier) {
        Column(modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombre,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isTablet) 18.sp else 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (observacion.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Nota",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = observacion,
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (modificadores.isNotEmpty()) {
                        Text(
                            text = modificadores.joinToString(", "),
                            fontSize = if (isTablet) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }
            }
            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Selector de cantidad sutil
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { onCantidadChange(cantidad - 1) },
                        modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                    ) { 
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp)) 
                    }
                    
                    Text(
                        text = "$cantidad",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                    
                    IconButton(
                        onClick = { onCantidadChange(cantidad + 1) },
                        modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                    ) { 
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) 
                    }
                }

                // Precio dominante
                Text(
                    text = "$${String.format(Locale.US, "%.2f", subtotal)}",
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ResumenVentaRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
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
                modificadores = listOf("Sin cebolla", "Extra queso"),
                onCantidadChange = {},

            )
        }
    }
}
