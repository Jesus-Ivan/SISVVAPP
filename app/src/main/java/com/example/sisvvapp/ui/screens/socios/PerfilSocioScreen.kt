package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.*

@Composable
fun PerfilSocioScreen(
    socio: SocioEntity,
    integrantes: List<IntegranteEntity>,
    onBackClick: () -> Unit
) {
    VistaVerdeScaffold(
        title = "Perfil del socio",
        onMenuClick = onBackClick,
        isBackButton = true,
        showConnectionBanner = true
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    VistaVerdeSectionHeader(text = "Detalles")
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VistaVerdeAvatar(modifier = Modifier.size(80.dp))

                            Spacer(modifier = Modifier.height(16.dp))

                            val esActivo = socio.estatus.equals("Activo", ignoreCase = true)
                            VistaVerdeStatusBadge(
                                text = socio.estatus,
                                containerColor = if (esActivo) FondoAbierta else Color(0xFFFFEBEE), // 2. CORRECCIÓN: Color Hex directo
                                textColor = if (esActivo) TextoAbierta else Color(0xFFD32F2F)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "#${socio.id} - ${socio.nombre} ${socio.apellidoP}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextoPrincipalClaro
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (socio.firmaAutorizada) {
                                Text(
                                    text = "Firma Autorizada",
                                    fontSize = 13.sp,
                                    color = VerdePrincipal,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Membresía ${socio.membresiaTipo}",
                                fontSize = 13.sp,
                                color = TextoSecundarioClaro
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    VistaVerdeSectionHeader(text = "Integrantes")
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(
                    items = integrantes,
                    key = { it.id }
                ) { integrante ->
                    IntegranteCard(integrante)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun IntegranteCard(integrante: IntegranteEntity) {
    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "#${integrante.id} - ${integrante.nombre}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextoPrincipalClaro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = integrante.parentesco,
                    fontSize = 13.sp,
                    color = TextoSecundarioClaro
                )
            }
            VistaVerdeAvatar()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilSocioScreenPreview() {
    val mockSocio = SocioEntity(
        id = 1832, nombre = "Cristian", apellidoP = "Meza", apellidoM = "",
        telefono = null, email = null, firmaAutorizada = true,
        estatus = "Activo", fotoUrl = "", membresiaTipo = "Familiar"
    )

    val mockIntegrantes = listOf(
        // 3. CORRECCIÓN: Agregamos fotoUrl = "" a los integrantes del Preview
        IntegranteEntity(id = 1833, socioId = 1832, nombre = "Alejandro Ramírez", parentesco = "Hijo", fotoUrl = ""),
        IntegranteEntity(id = 1834, socioId = 1832, nombre = "David López", parentesco = "Hijo", fotoUrl = "")
    )

    SISVVAPPTheme {
        PerfilSocioScreen(
            socio = mockSocio,
            integrantes = mockIntegrantes,
            onBackClick = {}
        )
    }
}