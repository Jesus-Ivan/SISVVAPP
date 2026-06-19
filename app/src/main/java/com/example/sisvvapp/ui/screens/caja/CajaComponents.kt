package com.example.sisvvapp.ui.screens.caja

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType

@Composable
fun VistaVerdeCajaCard(
    caja: CajaDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    VistaVerdeBaseCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${caja.id} - ${caja.nombre}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isTablet) 17.sp else 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 4.dp))
                Text(
                    text = "${caja.fechaApertura}",
                    fontWeight = FontWeight.Medium,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun CajasList(cajas: List<CajaDto>, selectedCajaId: Int?, onCajaClick: (CajaDto) -> Unit) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (isTablet) 2 else 1),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = cajas, key = { caja -> caja.id }) { caja ->
            VistaVerdeCajaCard(
                caja = caja,
                isSelected = caja.id == selectedCajaId,
                onClick = { onCajaClick(caja) }
            )
        }
    }
}

// --- PREVIEWS CONFIGURADOS PARA MODO OSCURO ---

@Preview(showBackground = true, name = "Modo Claro")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Modo Oscuro")
@Composable
fun CajaCardPreview() {
    SISVVAPPTheme {
        val mockCaja = CajaDto(6858, "Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123)
        Surface(modifier = Modifier.padding(16.dp)) {
            VistaVerdeCajaCard(caja = mockCaja, isSelected = true, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Lista Modo Claro")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Lista Modo Oscuro")
@Composable
fun CajasListPreview() {
    SISVVAPPTheme {
        val mockCajas = listOf(
            CajaDto(6858, "Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123),
            CajaDto(6857, "Barra/Restaurant", "Sáb 05 / May / 2026 15:23:08", null, true, 123)
        )

        Surface(modifier = Modifier.fillMaxSize()) {
            CajasList(
                cajas = mockCajas,
                selectedCajaId = 6858,
                onCajaClick = {}
            )
        }
    }
}