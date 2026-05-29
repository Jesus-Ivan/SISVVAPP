package com.example.sisvvapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaVerdeTopBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBackButton: Boolean = false,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showNavigationIcon: Boolean = true // 1. Agregamos "= true" para no romper las otras pantallas
) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            // 2. Envolvemos el botón en el IF
            if (showNavigationIcon) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = if (isBackButton) Icons.Default.ArrowBack else Icons.Default.Menu,
                        contentDescription = if (isBackButton) "Regresar" else "Menú principal",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    MaterialTheme.colorScheme.background
                )
            )
        )
    )
}

// --- PREVIEWS ---
@Preview(showBackground = true)
@Composable
fun TopBarPreviewSinIconos() {
    SISVVAPPTheme {
        VistaVerdeTopBar(
            title = "Ajustes",
            onMenuClick = {},
            subtitle = "Configuración y sincronización",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreviewConBotonRegreso() {
    SISVVAPPTheme {
        VistaVerdeTopBar(
            title = "Perfil del socio",
            onMenuClick = {},
            isBackButton = true,
            subtitle = "Información detallada",
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
            subtitle = "Gestiona las comandas del día",
            actions = {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            },
        )
    }
}