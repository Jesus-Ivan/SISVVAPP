package com.example.sisvvapp.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VistaVerdeConnectivityBanner(
    isOnline: Boolean
) {
    var showConnected by remember { mutableStateOf(false) }

    // Lógica para mostrar "Conectado" solo temporalmente cuando vuelve la red
    LaunchedEffect(isOnline) {
        if (isOnline) {
            showConnected = true
            delay(3000) // Se oculta tras 3 segundos
            showConnected = false
        } else {
            showConnected = false
        }
    }

    val isVisible = !isOnline || showConnected

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val backgroundColor = if (isOnline) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }

        val textColor = if (isOnline) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

        val message = if (isOnline) {
            stringResource(R.string.banner_connected)
        } else {
            stringResource(R.string.banner_offline)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 6.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = textColor,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun VistaVerdeBanner(
    text: String,
    isError: Boolean = false
) {
    val backgroundColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    
    val textColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BannerPreview() {
    SISVVAPPTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado conectado
            VistaVerdeBanner(
                text = "Conectado al servidor",
                isError = false
            )

            // Estado sin conexión
            VistaVerdeBanner(
                text = "Sin conexión al servidor",
                isError = true
            )
        }
    }
}
