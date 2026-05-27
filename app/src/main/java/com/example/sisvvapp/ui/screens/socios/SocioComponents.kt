package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.VistaVerdeAvatar
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeStatusBadge
import com.example.sisvvapp.ui.theme.*

@Composable
fun VistaVerdeSocioCard(
    socio: SocioEntity,
    modifier: Modifier = Modifier
) {
    val isActive = socio.estatus == "MEN"
    val membresiaTexto = socio.membresiaTipo ?: "Sin membresía"

    VistaVerdeBaseCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${socio.id} - ${socio.nombre} ${socio.apellidoP}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (socio.firmaAutorizada == true) {
                    Text(
                        text = "Firma Autorizada",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = membresiaTexto,
                    fontFamily = Inter,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VistaVerdeAvatar(fotoUrl = socio.fotoUrl)
                Spacer(modifier = Modifier.height(8.dp))
                VistaVerdeStatusBadge(
                    text = if (isActive) "PERMITIDO" else "DENEGADO",
                    containerColor = if (isActive) EstadoExitoClaro else FondoErrorClaro,
                    textColor = if (isActive) EstadoExitoOscuro else TextoErrorFuerte
                )
            }
        }
    }
}

@Composable
private fun SocioCardItem(socio: SocioEntity, onSocioClick: (SocioEntity) -> Unit) {
    VistaVerdeSocioCard(
        socio = socio,
        modifier = Modifier.clickable { onSocioClick(socio) }
    )
}

@Composable
fun SociosList(socios: List<SocioEntity>, onSocioClick: (SocioEntity) -> Unit) {
    BoxWithConstraints {
        if (maxWidth > 600.dp) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(socios, key = { it.id }) { socio ->
                    SocioCardItem(socio = socio, onSocioClick = onSocioClick)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(socios, key = { it.id }) { socio ->
                    SocioCardItem(socio = socio, onSocioClick = onSocioClick)
                }
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun SocioCardPreview() {
    SISVVAPPTheme {
        val mockSocio = SocioEntity(
            id = 1,
            nombre = "Cristian",
            apellidoP = "Meza",
            apellidoM = "",
            telefono = null,
            email = null,
            firmaAutorizada = true,
            estatus = "MEN",
            fotoUrl = "",
            numAccion = null,
            membresiaTipo = "Familiar"
        )

        Surface(modifier = Modifier.padding(16.dp)) {
            VistaVerdeSocioCard(socio = mockSocio)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SociosListPreview() {
    SISVVAPPTheme {
        val mockSocios = listOf(
            SocioEntity(
                id = 1,
                nombre = "Cristian",
                apellidoP = "Meza",
                apellidoM = "",
                telefono = null,
                email = null,
                firmaAutorizada = true,
                estatus = "MEN",
                fotoUrl = "",
                numAccion = null,
                membresiaTipo = "Parejas"
            ),
            SocioEntity(
                id = 2,
                nombre = "Juan",
                apellidoP = "Pérez",
                apellidoM = "López",
                telefono = null,
                email = null,
                firmaAutorizada = false,
                estatus = "CAN",
                fotoUrl = "",
                numAccion = null,
                membresiaTipo = "Familiar"
            )
        )

        Surface(modifier = Modifier.fillMaxSize()) {
            SociosList(socios = mockSocios, onSocioClick = {})
        }
    }
}
