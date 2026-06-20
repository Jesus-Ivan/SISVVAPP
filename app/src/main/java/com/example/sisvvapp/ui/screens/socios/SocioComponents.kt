package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.VistaVerdeAvatar
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeStatusBadge
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType

@Composable
fun VistaVerdeSocioCard(
    socio: SocioEntity,
    onNuevaVentaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Es "PERMITIDO" si tiene una membresía activa (MEN o ANU)
    // El Mapper ya se encarga de que estatus sea MEN o ANU si hay alguna activa
    val isActive = socio.estatus == "MEN" || socio.estatus == "ANU"
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#${socio.id} - ${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}".trimEnd(),
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
                Text(
                    text = membresiaTexto,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                IconButton(
                    onClick = { onNuevaVentaClick() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Nueva Venta",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            // --- COLUMNA DERECHA ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VistaVerdeAvatar(fotoUrl = socio.fotoUrl)
                Spacer(modifier = Modifier.height(8.dp))
                VistaVerdeStatusBadge(
                    text = if (isActive) "PERMITIDO" else "DENEGADO",
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    textColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun SociosList(
    socios: List<SocioEntity>,
    onSocioClick: (SocioEntity) -> Unit,
    onNuevaVentaClick: (SocioEntity) -> Unit
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET
    val gridState = rememberLazyGridState()
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        snapshotFlow { gridState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling) keyboardController?.hide()
            }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(if (isTablet) 2 else 1),
        contentPadding = PaddingValues(vertical = 16.dp),
        // Espaciado vertical entre tarjetas
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = socios, key = { socio -> socio.id }) { socio ->
            VistaVerdeSocioCard(
                socio = socio,
                onNuevaVentaClick = { onNuevaVentaClick(socio) },
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
            id = 1, nombre = "Cristian", apellidoP = "Meza", apellidoM = "",
            telefono = null, email = null, firmaAutorizada = true,
            estatus = "MEN", fotoUrl = "", numAccion = null, membresiaTipo = "Familiar"
        )
        Surface(modifier = Modifier.padding(16.dp)) {
            VistaVerdeSocioCard(socio = mockSocio, onNuevaVentaClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SociosListPreview() {
    SISVVAPPTheme {
        val mockSocios = listOf(
            SocioEntity(
                id = 1, nombre = "Cristian", apellidoP = "Meza", apellidoM = "",
                telefono = null, email = null, firmaAutorizada = true,
                estatus = "MEN", fotoUrl = "", numAccion = null, membresiaTipo = "Parejas"
            ),
            SocioEntity(
                id = 2, nombre = "Juan", apellidoP = "Pérez", apellidoM = "López",
                telefono = null, email = null, firmaAutorizada = false,
                estatus = "CAN", fotoUrl = "", numAccion = null, membresiaTipo = "Familiar"
            )
        )
        Surface(modifier = Modifier.fillMaxSize()) {
            SociosList(socios = mockSocios, onSocioClick = {}, onNuevaVentaClick = {})
        }
    }
}
