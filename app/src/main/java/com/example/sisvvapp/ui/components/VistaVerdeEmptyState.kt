package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal

@Composable
fun VistaVerdeEmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Estado vacío",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontFamily = Inter,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onActionClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VerdePrincipal
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateSearchPreview() {
    SISVVAPPTheme {
        VistaVerdeEmptyState(
            icon = Icons.Default.SearchOff,
            message = "No se encontraron socios con ese nombre o número."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateErrorPreview() {
    SISVVAPPTheme {
        VistaVerdeEmptyState(
            icon = Icons.Default.WifiOff,
            message = "Hubo un problema al conectar con el servidor.",
            actionText = "Reintentar",
            onActionClick = {}
        )
    }
}