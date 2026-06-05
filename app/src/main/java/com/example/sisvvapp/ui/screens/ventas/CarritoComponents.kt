package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import java.util.Locale

@Composable
fun CarritoItemCard(
    nombre: String,
    cantidad: Int,
    subtotal: Double,
    modificadores: List<String> = emptyList(),
    onCantidadChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    VistaVerdeBaseCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = nombre, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (modificadores.isNotEmpty()) {
                        Text(text = modificadores.joinToString(", "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onCantidadChange(cantidad - 1) }, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
                    Text(text = "$cantidad", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { onCantidadChange(cantidad + 1) }, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
                }
                Text(text = "$${String.format(Locale.US, "%.2f", subtotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
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
                modificadores = listOf("Sin cebolla", "Extra queso"),
                onCantidadChange = {},

            )
        }
    }
}
