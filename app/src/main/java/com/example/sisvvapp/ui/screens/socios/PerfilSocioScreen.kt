package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.*
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun PerfilSocioScreen(
    socio: SocioEntity,
    integrantes: List<IntegranteEntity>,
    isOnline: Boolean = true,
    onNuevaVentaClick: (SocioEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    var photoToShow by remember { mutableStateOf<String?>(null) }
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        ) {
            VistaVerdeScaffold(
                title = stringResource(R.string.perfil_socio_title),
                onMenuClick = onBackClick,
                isBackButton = true,
                isOnline = isOnline
            ) {
                ResponsiveContainer() {
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
                                VistaVerdeSectionHeader(text = stringResource(R.string.perfil_socio_section_details))
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = if (isTablet) 28.dp else 24.dp, horizontal = if (isTablet) 20.dp else 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        VistaVerdeAvatar(
                                            fotoUrl = socio.fotoUrl,
                                            modifier = Modifier
                                                .size(114.dp)
                                                .clickable { photoToShow = socio.fotoUrl }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = listOfNotNull(
                                                socio.nombre,
                                                socio.apellidoP.takeIf { it.isNotBlank() },
                                                socio.apellidoM.takeIf { it.isNotBlank() }
                                            ).joinToString(" "),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (isTablet) 22.sp else 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "No.Socio: ${socio.id}",
                                            fontSize = if (isTablet) 15.sp else 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // --- NUEVA SECCIÓN DE MEMBRESÍAS (HISTORIAL) ---
                                        val gson = remember { com.google.gson.Gson() }
                                        val membresias = remember(socio.membresiasJson) {
                                            try {
                                                val type = object : com.google.gson.reflect.TypeToken<List<com.example.sisvvapp.network.dto.socios.MembresiaDto>>() {}.type
                                                gson.fromJson<List<com.example.sisvvapp.network.dto.socios.MembresiaDto>>(socio.membresiasJson, type) ?: emptyList()
                                            } catch (e: Exception) {
                                                emptyList()
                                            }
                                        }

                                        if (membresias.isNotEmpty()) {
                                            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                            androidx.compose.foundation.layout.FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                maxItemsInEachRow = 2
                                            ) {
                                                membresias.filter { it.estado != "CAN" }.forEach { m ->
                                                    MembresiaBadge(
                                                        clave = m.clave ?: "???",
                                                        estado = m.estado ?: "???",
                                                        isTablet = isTablet
                                                    )
                                                }
                                            }
                                        } else {
                                            // Fallback si no hay JSON (compatibilidad con datos viejos)
                                            MembresiaBadge(
                                                clave = socio.membresiaTipo ?: "Sin membresía",
                                                estado = socio.estatus,
                                                isTablet = isTablet
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        if (socio.estatus != "CANCELADO") {
                                            VistaVerdeStatusBadge(
                                                text = stringResource(R.string.perfil_socio_acceso_permitido),
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                textColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        } else {
                                            VistaVerdeStatusBadge(
                                                text = stringResource(R.string.perfil_socio_acceso_denegado),
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                textColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        val firmaColor = if (socio.firmaAutorizada) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                        
                                        Text(
                                            text = if (socio.firmaAutorizada)
                                                stringResource(R.string.perfil_socio_firma_autorizada)
                                            else
                                                stringResource(R.string.perfil_socio_firma_no_autorizada),
                                            fontSize = if (isTablet) 15.sp else 13.sp,
                                            color = firmaColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                VistaVerdeSectionHeader(text = stringResource(R.string.perfil_socio_section_members))
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            items(
                                items = integrantes,
                                key = { it.id }
                            ) { integrante ->
                                IntegranteCard(
                                    integrante = integrante,
                                    isTablet = isTablet,
                                    onPhotoClick = { url -> photoToShow = url }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        PhotoViewerOverlay(
            fotoUrl = photoToShow,
            hazeState = hazeState,
            onDismiss = { photoToShow = null }
        )

        FloatingActionButton(
            onClick = { onNuevaVentaClick(socio) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddShoppingCart,
                contentDescription = "Nueva Venta",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun IntegranteCard(
    integrante: IntegranteEntity,
    isTablet: Boolean = false,
    onPhotoClick: (String) -> Unit = {}
) {
    VistaVerdeBaseCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val nombreCompleto = buildString {
                    append(integrante.nombre)
                    if (!integrante.apellidoP.isNullOrBlank()) append(" ${integrante.apellidoP}")
                    if (!integrante.apellidoM.isNullOrBlank()) append(" ${integrante.apellidoM}")
                }.ifBlank { integrante.parentesco }
                Text(
                    text = nombreCompleto,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 17.sp else 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 4.dp))
                Text(
                    text = integrante.parentesco,
                    fontSize = if (isTablet) 15.sp else 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!integrante.fotoUrl.isNullOrBlank()) {
                VistaVerdeAvatar(
                    fotoUrl = integrante.fotoUrl,
                    modifier = Modifier.clickable { onPhotoClick(integrante.fotoUrl) }
                )
            }
        }
    }
}

@Composable
fun MembresiaBadge(
    clave: String,
    estado: String,
    isTablet: Boolean
) {
    val (containerColor, contentColor, label) = when (estado) {
        "MEN", "ANU" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "ACTIVA"
        )
        "INA" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "INACTIVA"
        )
        else -> Triple(
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            estado
        )
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = contentColor.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(contentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = clave,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = label,
                    fontSize = if (isTablet) 11.sp else 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.7f),
                    lineHeight = 10.sp
                )
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
        IntegranteEntity(id = 1833, socioId = 1832, nombre = "Alejandro Ramírez", apellidoP = null, apellidoM = null, parentesco = "Hijo", fotoUrl = ""),
        IntegranteEntity(id = 1834, socioId = 1832, nombre = "David López", apellidoP = null, apellidoM = null, parentesco = "Hijo", fotoUrl = "")
    )

    SISVVAPPTheme {
        PerfilSocioScreen(
            socio = mockSocio,
            integrantes = mockIntegrantes,
            onNuevaVentaClick = {},
            onBackClick = {}
        )
    }
}
