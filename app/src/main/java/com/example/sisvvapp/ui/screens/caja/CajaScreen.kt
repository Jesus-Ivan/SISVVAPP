package com.example.sisvvapp.ui.screens.caja

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
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
import androidx.compose.ui.res.stringResource
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.LocalScaleFactor
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal

@Composable
fun CajaScreen(
    cajas: List<CajaDto>,
    selectedCajaId: Int?,
    isLoading: Boolean,
    isOnline: Boolean = true,

    onCajaClick: (Int) -> Unit,
    onMenuClick: () -> Unit,
    onContinueClick: (Int) -> Unit
) {
    val scale = LocalScaleFactor.current
    VistaVerdeScaffold(
        title = stringResource(R.string.caja_section_open),
        onMenuClick = onMenuClick,
        isOnline = isOnline
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp * scale)
        ) {
            Spacer(modifier = Modifier.height(16.dp * scale))
            VistaVerdeSectionHeader(text = stringResource(R.string.caja_title_select))
            Spacer(modifier = Modifier.height(8.dp * scale))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VerdePrincipal)
                }
            } else if (cajas.isEmpty()) {
                VistaVerdeEmptyState(
                    icon = Icons.Default.Inbox,
                    message = stringResource(R.string.caja_empty_state),
                    modifier = Modifier.weight(1f)
                )
            } else {
                CajasList(
                    cajas = cajas,
                    selectedCajaId = selectedCajaId,
                    onCajaClick = { caja ->
                        onCajaClick(caja.id)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp * scale))

            Button(
                onClick = { selectedCajaId?.let { onContinueClick(it) } },
                enabled = selectedCajaId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp * scale),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.caja_button_continue),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp * scale,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(32.dp * scale))
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