package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeSaleCard
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun VentasScreen(
    onMenuClick: () -> Unit
) {
    VistaVerdeScaffold(
        title = "Ventas",
        onMenuClick = onMenuClick,
        actions = {
            // Los iconos deben ir DENTRO de estas llaves
            IconButton(onClick = { /* Acción búsqueda */ }) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
            IconButton(onClick = { /* Acción calendario */ }) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendario")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {


            }
        }
    }



    @Preview(showBackground = true)
    @Composable
    fun VentasScreenPreview() {
        SISVVAPPTheme {
            VentasScreen(
                onMenuClick = {}
            )
        }
    }