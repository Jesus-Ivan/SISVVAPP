package com.example.sisvvapp.ui.screens.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.screens.caja.VistaVerdeCajaCard
import com.example.sisvvapp.ui.theme.LocalScaleFactor
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.TextoSecundarioClaro
import com.example.sisvvapp.ui.theme.VerdePrincipal

@Composable
fun AjustesScreen(
    cajas: List<CajaDto>,
    selectedCajaId: Int?,
    lastSyncDate: String,
    isOnline: Boolean = true,
    onCajaClick: (CajaDto) -> Unit,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val scale = LocalScaleFactor.current
    VistaVerdeScaffold(
        title = stringResource(id = R.string.ajustes_title),
        onMenuClick = onMenuClick,
        isOnline = isOnline
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp * scale, vertical = 16.dp * scale),
            verticalArrangement = Arrangement.spacedBy(16.dp * scale)
        ) {

            // --- 1. SECCIÓN: CAJAS ABIERTAS ---
            item {
                VistaVerdeSectionHeader(text = stringResource(id = R.string.ajustes_cajas_abiertas))
            }

            if (cajas.isEmpty()) {
                item {
                VistaVerdeEmptyState(
                    icon = Icons.Default.Inbox,
                    message = stringResource(R.string.ajustes_cajas_empty),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp * scale)
                )
                }
            } else {
                items(items = cajas, key = { it.id }) { caja ->
                    VistaVerdeCajaCard(
                        caja = caja,
                        isSelected = caja.id == selectedCajaId,
                        onClick = { onCajaClick(caja) }
                    )
                }
            }

            // --- 2. SECCIÓN: SINCRONIZAR DATOS ---
            item {
                Spacer(modifier = Modifier.height(8.dp * scale))
                VistaVerdeSectionHeader(text = stringResource(id = R.string.ajustes_sincronizar_datos))
            }

            item {
                VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp * scale),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.ajustes_sync_desc),
                            fontSize = 13.sp * scale,
                            color = TextoSecundarioClaro,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp * scale)
                        )

                        Button(
                            onClick = onSyncClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp * scale),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.ajustes_btn_actualizar),
                                modifier = Modifier.padding(end = 8.dp * scale)
                            )
                            Text(
                                text = stringResource(id = R.string.ajustes_btn_actualizar),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp * scale,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp * scale))

                        Text(
                            text = stringResource(id = R.string.ajustes_ultima_act, lastSyncDate),
                            fontSize = 12.sp * scale,
                            color = TextoSecundarioClaro,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp * scale
                        )
                    }
                }
            }

            // --- 3. SECCIÓN: CERRAR SESIÓN ---
            item {
                Spacer(modifier = Modifier.height(8.dp * scale))
                VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp * scale),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = BorderStroke(0.dp, Color.Transparent),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(id = R.string.ajustes_btn_cerrar_sesion),
                            modifier = Modifier.padding(end = 8.dp * scale)
                        )
                        Text(
                            text = stringResource(id = R.string.ajustes_btn_cerrar_sesion),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp * scale
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AjustesScreenPreview() {
    val mockCajas = listOf(
        CajaDto(6858, "Bar", "Sáb 05 / May / 2026 15:23:08", null, true, 123)
    )

    SISVVAPPTheme {
        AjustesScreen(
            cajas = mockCajas,
            selectedCajaId = 6858,
            lastSyncDate = "Hoy 11:24 AM",
            onCajaClick = {},
            onSyncClick = {},
            onLogoutClick = {},
            onMenuClick = {}
        )
    }
}
