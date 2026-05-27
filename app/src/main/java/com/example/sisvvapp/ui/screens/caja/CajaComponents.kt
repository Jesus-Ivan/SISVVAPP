package com.example.sisvvapp.ui.screens.caja

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.theme.LocalScaleFactor
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard

@Composable
fun VistaVerdeCajaCard(
    caja: CajaDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale = LocalScaleFactor.current
    VistaVerdeBaseCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp * scale),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${caja.id} - ${caja.nombre}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp * scale,
                    color = TextoPrincipalClaro
                )
                Spacer(modifier = Modifier.height(4.dp * scale))
                Text(
                    text = "${caja.fechaApertura}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp * scale,
                    color = TextoSecundarioClaro
                )
                Text(
                    text = "Cajero: ${caja.meseroId ?: "N/A"}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp * scale,
                    color = TextoSecundarioClaro
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}

@Composable
fun CajasList(cajas: List<CajaDto>, selectedCajaId: Int?, onCajaClick: (CajaDto) -> Unit) {
    val scale = LocalScaleFactor.current
    LazyColumn(
        contentPadding = PaddingValues(16.dp * scale),
        verticalArrangement = Arrangement.spacedBy(12.dp * scale)
    ) {
        items(items= cajas, key ={caja -> caja.id}) { caja ->
            VistaVerdeCajaCard(
                caja = caja,
                isSelected = caja.id == selectedCajaId,
                onClick = { onCajaClick(caja) }
            )
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun CajaCardPreview() {
    SISVVAPPTheme {
        val mockCaja = CajaDto(6858, "Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123)
        Surface(modifier = Modifier.padding(16.dp)) {
            VistaVerdeCajaCard(caja = mockCaja, isSelected = true, onClick = {})
        }
    }
}

// --- PREVIEW DE LA LISTA ---

@Preview(showBackground = true)
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