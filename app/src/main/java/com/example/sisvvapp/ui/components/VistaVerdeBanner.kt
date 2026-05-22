package com.example.sisvvapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.theme.EstadoAlertaClaro
import com.example.sisvvapp.ui.theme.EstadoAlertaOscuro
import com.example.sisvvapp.ui.theme.EstadoExitoClaro
import com.example.sisvvapp.ui.theme.EstadoExitoOscuro
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun VistaVerdeBanner(
    text: String,
    isError: Boolean = false
){
    val backgroundColor = if (isError) {
        EstadoAlertaClaro
    } else EstadoExitoClaro
    val textColor = if(isError) {
        EstadoAlertaOscuro
    } else EstadoExitoOscuro

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