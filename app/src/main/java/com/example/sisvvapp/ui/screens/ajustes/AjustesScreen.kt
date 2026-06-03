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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.components.VistaVerdeSkeletonCard
import com.example.sisvvapp.ui.screens.caja.VistaVerdeCajaCard
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun AjustesScreen(
    cajas: List<CajaDto>,
    selectedCajaId: Int?,
    lastSyncDate: String,
    isLoading: Boolean = false,
    isOnline: Boolean = true,
    onCajaClick: (CajaDto) -> Unit,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    VistaVerdeScaffold(
        title = stringResource(id = R.string.ajustes_title),
        subtitle = stringResource(id = R.string.ajustes_subtitle),
        onMenuClick = onMenuClick,
        isOnline = isOnline,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar cajas")
            }
        }
    ) {
        ResponsiveContainer {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- 1. SECCIÓN: CAJAS ABIERTAS ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VistaVerdeSectionHeader(text = stringResource(id = R.string.ajustes_cajas_abiertas))
                    }
                }

                // LÓGICA DE ESTADOS MODIFICADA
                if (isLoading) {
                    // ESTADO 1: Cargando
                    items(3) {
                        VistaVerdeSkeletonCard()
                    }
                } else if (cajas.isEmpty()) {
                    // ESTADO 2: Lista vacía
                    item {
                        VistaVerdeEmptyState(
                            icon = Icons.Default.Inbox,
                            message = stringResource(R.string.ajustes_cajas_empty),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                } else {
                    // ESTADO 3: Lista real de cajas
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
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeSectionHeader(text = stringResource(id = R.string.ajustes_sincronizar_datos))
                }

                item {
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.ajustes_sync_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Button(
                                onClick = onSyncClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(id = R.string.ajustes_btn_actualizar),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.ajustes_btn_actualizar),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(id = R.string.ajustes_ultima_act, lastSyncDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // --- 3. SECCIÓN: CERRAR SESIÓN ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(0.dp, MaterialTheme.colorScheme.surface),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(id = R.string.ajustes_btn_cerrar_sesion),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.ajustes_btn_cerrar_sesion),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
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