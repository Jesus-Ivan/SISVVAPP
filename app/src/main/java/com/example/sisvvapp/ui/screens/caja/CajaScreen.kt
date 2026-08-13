package com.example.sisvvapp.ui.screens.caja

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.R
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.components.VistaVerdeSkeletonCard

@Composable
fun CajaScreen(
    cajas: List<CajaDto>,
    selectedCajaId: Int?,
    isLoading: Boolean,
    isOnline: Boolean = true,
    isFromSettings: Boolean = false,
    errorMessage: String? = null,
    onCajaClick: (Int) -> Unit,
    onMenuClick: () -> Unit = {},
    onNavigationClick: () -> Unit = {},
    onContinueClick: (Int) -> Unit,
    onRetry: () -> Unit = {}
) {
    VistaVerdeScaffold(
        title = stringResource(R.string.caja_title),
        onMenuClick = if (isFromSettings) onMenuClick else onNavigationClick,
        isBackButton = isFromSettings,
        showNavigationIcon = isFromSettings,
        isOnline = isOnline,
        actions = {
            IconButton(onClick = onRetry, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar cajas")
            }
        }
    ) {
        ResponsiveContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                VistaVerdeSectionHeader(text = stringResource(R.string.caja_section_open))
                Spacer(modifier = Modifier.height(8.dp))


                if (isLoading) {
                    // ESTADO 1: Cargando (Mostramos Skeletons)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(3) {
                            VistaVerdeSkeletonCard()
                        }
                    }
                } else if (errorMessage != null) {
                    // ESTADO 2: Error
                    VistaVerdeEmptyState(
                        icon = Icons.Default.Inbox,
                        message = errorMessage,
                        modifier = Modifier.weight(1f),
                        actionText = "Reintentar",
                        onActionClick = onRetry
                    )
                } else if (cajas.isEmpty()) {
                    // ESTADO 3: Vacío
                    VistaVerdeEmptyState(
                        icon = Icons.Default.Inbox,
                        message = "No hay cajas abiertas disponibles.",
                        modifier = Modifier.weight(1f),
                        actionText = "Reintentar",
                        onActionClick = onRetry
                    )
                } else {
                    // ESTADO 4: Lista real
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CajasList(
                            cajas = cajas,
                            selectedCajaId = selectedCajaId,
                            onCajaClick = { caja ->
                                onCajaClick(caja.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VistaVerdeButton(
                    text = "CONTINUAR",
                    onClick = { selectedCajaId?.let { onContinueClick(it) } },
                    enabled = selectedCajaId != null
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}