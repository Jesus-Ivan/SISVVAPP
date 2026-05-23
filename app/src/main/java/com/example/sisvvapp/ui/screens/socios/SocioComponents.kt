package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isActive = socio.estatus.equals("Activo", ignoreCase = true)
    val membresiaTexto = socio.membresiaTipo ?: "Sin membresía"

    VistaVerdeBaseCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- COLUMNA IZQUIERDA ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${socio.id} - ${socio.nombre} ${socio.apellidoP}",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (socio.firmaAutorizada == true) {
                    Text(
                        text = "Firma Autorizada",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                // CORRECCIÓN: Como la membresía está en otra tabla, usamos un texto fijo por ahora
                Text(
                    text = membresiaTexto,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            // --- COLUMNA DERECHA ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VistaVerdeAvatar()
                Spacer(modifier = Modifier.height(8.dp))
                VistaVerdeStatusBadge(
                    text = if (isActive) "Activo" else "Inactivo",
                    containerColor = if (isActive) EstadoExitoClaro else FondoInactivo,
                    textColor = if (isActive) EstadoExitoOscuro else TextoInactivo
                )
            }
        }
    }
}


@Composable
fun SociosList(socios: List<SocioEntity>, onSocioClick: (SocioEntity) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(vertical=16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items=socios, key={socio -> socio.id}) { socio ->
            VistaVerdeSocioCard(
                socio = socio,
                modifier = Modifier.clickable { onSocioClick(socio) }
            )
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
            estatus = "Activo",
            fotoUrl = "",
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
                estatus = "Activo",
                fotoUrl = "",
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
                estatus = "Inactivo",
                fotoUrl = "",
                membresiaTipo = "Familiar"
            )
        )

        Surface(modifier = Modifier.fillMaxSize()) {
            SociosList(socios = mockSocios, onSocioClick = {})
        }
    }
}