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
import androidx.compose.ui.res.stringResource
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.LocalScaleFactor
import com.example.sisvvapp.ui.theme.*

@Composable
fun PerfilSocioScreen(
    socio: SocioEntity,
    integrantes: List<IntegranteEntity>,
    isOnline: Boolean = true,
    onBackClick: () -> Unit
) {
    val scale = LocalScaleFactor.current
    VistaVerdeScaffold(
        title = stringResource(R.string.perfil_socio_title),
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = isOnline
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp * scale),
                contentPadding = PaddingValues(bottom = 32.dp * scale)
            ) {

                item {
                    Spacer(modifier = Modifier.height(16.dp * scale))
                    VistaVerdeSectionHeader(text = stringResource(R.string.perfil_socio_section_details))
                    Spacer(modifier = Modifier.height(16.dp * scale))
                }

                item {
                    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp * scale, horizontal = 16.dp * scale),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VistaVerdeAvatar(fotoUrl = socio.fotoUrl, modifier = Modifier.size(125.dp * scale))

                            Spacer(modifier = Modifier.height(16.dp * scale))

                            Text(
                                text = listOfNotNull(
                                    socio.nombre,
                                    socio.apellidoP.takeIf { it.isNotBlank() },
                                    socio.apellidoM.takeIf { it.isNotBlank() }
                                ).joinToString(" "),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp * scale,
                                color = TextoPrincipalClaro
                            )

                            Spacer(modifier = Modifier.height(12.dp * scale))

                            Text(
                                text = "Estado membresia: ${socio.estatus}",
                                fontSize = 13.sp * scale,
                                color = TextoSecundarioClaro
                            )

                            Spacer(modifier = Modifier.height(4.dp * scale))

                            Text(
                                text = "Tipo membresia: ${socio.membresiaTipo}",
                                fontSize = 13.sp * scale,
                                color = TextoSecundarioClaro
                            )

                            Spacer(modifier = Modifier.height(4.dp * scale))

                            Text(
                                text = "No.Socio: ${socio.id}",
                                fontSize = 13.sp * scale,
                                color = TextoSecundarioClaro
                            )

                            Spacer(modifier = Modifier.height(12.dp * scale))

                            if (socio.estatus == "CAN") {
                                VistaVerdeStatusBadge(
                                    text = stringResource(R.string.perfil_socio_acceso_denegado),
                                    containerColor = FondoErrorClaro,
                                    textColor = TextoErrorFuerte
                                )
                            } else {
                                VistaVerdeStatusBadge(
                                    text = stringResource(R.string.perfil_socio_acceso_permitido),
                                    containerColor = FondoAbierta,
                                    textColor = TextoAbierta
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp * scale))
                            Text(
                                text = if (socio.firmaAutorizada)
                                    stringResource(R.string.perfil_socio_firma_autorizada)
                                else
                                    stringResource(R.string.perfil_socio_firma_no_autorizada),
                                fontSize = 13.sp * scale,
                                color = if (socio.firmaAutorizada) VerdePrincipal else TextoSecundarioClaro,
                                fontWeight = if (socio.firmaAutorizada) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp * scale))
                    VistaVerdeSectionHeader(text = stringResource(R.string.perfil_socio_section_members))
                    Spacer(modifier = Modifier.height(12.dp * scale))
                }

                items(
                    items = integrantes,
                    key = { it.id }
                ) { integrante ->
                    IntegranteCard(integrante)
                    Spacer(modifier = Modifier.height(8.dp * scale))
                }
            }
        }
    }
}

@Composable
fun IntegranteCard(integrante: IntegranteEntity) {
    val scale = LocalScaleFactor.current
    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                val nombreCompleto = buildString {
                    append(integrante.nombre)
                    if (!integrante.apellidoP.isNullOrBlank()) append(" ${integrante.apellidoP}")
                    if (!integrante.apellidoM.isNullOrBlank()) append(" ${integrante.apellidoM}")
                }.ifBlank { integrante.parentesco }
                Text(
                    text = nombreCompleto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp * scale,
                    color = TextoPrincipalClaro
                )
                Spacer(modifier = Modifier.height(4.dp * scale))
                Text(
                    text = integrante.parentesco,
                    fontSize = 13.sp * scale,
                    color = TextoSecundarioClaro
                )
            }
            if (!integrante.fotoUrl.isNullOrBlank()) {
                VistaVerdeAvatar(fotoUrl = integrante.fotoUrl)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilSocioScreenPreview() {
    val mockSocio = SocioEntity(
        id = 1832, nombre = "Cristian", apellidoP = "Meza", apellidoM = "",
        telefono = null, email = null, firmaAutorizada = true,
        estatus = "Activo", fotoUrl = "", numAccion = null, membresiaTipo = "Familiar"
    )

    val mockIntegrantes = listOf(
        // 3. CORRECCIÓN: Agregamos fotoUrl = "" a los integrantes del Preview
        IntegranteEntity(id = 1833, socioId = 1832, nombre = "Alejandro Ramírez", apellidoP = null, apellidoM = null, parentesco = "Hijo", fotoUrl = ""),
        IntegranteEntity(id = 1834, socioId = 1832, nombre = "David López", apellidoP = null, apellidoM = null, parentesco = "Hijo", fotoUrl = "")
    )

    SISVVAPPTheme {
        PerfilSocioScreen(
            socio = mockSocio,
            integrantes = mockIntegrantes,
            onBackClick = {}
        )
    }
}