package com.example.sisvvapp.ui.screens.caja

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal

@Composable
fun CajaScreen(
    cajas: List<CajaDto>,
    selectedCajaId: Int?,
    isLoading: Boolean,

    onCajaClick: (Int) -> Unit,
    onMenuClick: () -> Unit,
    onContinueClick: (Int) -> Unit
) {
    VistaVerdeScaffold(
        title = "Selecciona el corte de caja",
        onMenuClick = onMenuClick,
        showConnectionBanner = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            VistaVerdeSectionHeader(text = "Cajas abiertas")
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading && cajas.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    CajasList(
                        cajas = cajas, // Pasamos directamente la lista de DTOs
                        selectedCajaId = selectedCajaId,
                        onCajaClick = { caja -> onCajaClick(caja.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { selectedCajaId?.let { onContinueClick(it) } },
                enabled = selectedCajaId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Continuar →",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CajaScreenPreview() {
    val mockCajas = listOf(
        CajaDto(6858, "Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123),
        CajaDto(6857, "Barra/Restaurant", "Sáb 05 / May / 2026 15:23:08", null, true, 123),
        CajaDto(6856, "Cafetería", "Sáb 05 / May / 2026 15:23:08", null, true, 123),
        CajaDto(6855, "Caddie Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123)
    )

    SISVVAPPTheme {
        CajaScreen(
            cajas = mockCajas,
            selectedCajaId = 6858,
            isLoading = false,
            onCajaClick = {},
            onMenuClick = {},
            onContinueClick = {}
        )
    }
}