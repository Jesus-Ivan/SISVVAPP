package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaVerdeTopBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú principal",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = actions,
        // Usamos la nueva versión "topAppBarColors" para quitar la advertencia
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

// --- PREVIEWS ---
@Preview(showBackground = true)
@Composable
fun TopBarPreviewSinIconos() {
    SISVVAPPTheme {
        VistaVerdeTopBar(
            title = "Ajustes",
            onMenuClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreviewUnIcono() {
    SISVVAPPTheme {
        VistaVerdeTopBar(
            title = "Socios",
            onMenuClick = {},
            actions = {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreviewDosIconos() {
    SISVVAPPTheme {
        VistaVerdeTopBar(
            title = "Ventas",
            onMenuClick = {},
            actions = {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            }
        )
    }
}