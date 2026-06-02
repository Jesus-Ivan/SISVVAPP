package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun SociosScreen(
    socios: List<SocioEntity>,
    isLoading: Boolean,
    isOnline: Boolean = true,
    searchQuery: String,
    errorMessage: String? = null,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onSocioClick: (Int) -> Unit,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    VistaVerdeScaffold(
        title = stringResource(R.string.title_socios),
        subtitle = stringResource(R.string.socios_subtitle),
        onMenuClick = onMenuClick,
        isOnline = isOnline,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar socios")
            }
        }
    ) {
        ResponsiveContainer {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VistaVerdeSectionHeader(text = stringResource(R.string.socios_section_search))

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VistaVerdeSearchBar(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = stringResource(R.string.socios_search_placeholder)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // LÓGICA DE ESTADOS
                if (isLoading && socios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (errorMessage != null && socios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                } else if (socios.isEmpty()) {
                    VistaVerdeEmptyState(
                        icon = Icons.Default.SearchOff,
                        message = if (searchQuery.isEmpty())
                            stringResource(R.string.socios_empty_state)
                        else
                            stringResource(R.string.socios_empty_search_results, searchQuery),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    SociosList(
                        socios = socios,
                        onSocioClick = { socio ->
                            onSocioClick(socio.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SociosScreenPreview() {
    val mockSocios = listOf(
        SocioEntity(
            id = 1, nombre = "Cristian", apellidoP = "Meza", apellidoM = "García",
            telefono = null, email = null, firmaAutorizada = true,
            estatus = "Activo", fotoUrl = "", numAccion = null, membresiaTipo = "Gold"
        ),
        SocioEntity(
            id = 2, nombre = "Juan", apellidoP = "Pérez", apellidoM = "López",
            telefono = null, email = null, firmaAutorizada = false,
            estatus = "Inactivo", fotoUrl = "", numAccion = null, membresiaTipo = "Silver"
        )
    )

    SISVVAPPTheme {
        SociosScreen(
            socios = mockSocios,
            isLoading = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onMenuClick = {},
            onSocioClick = {}
        )
    }
}