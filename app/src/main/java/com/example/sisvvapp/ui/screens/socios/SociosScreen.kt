package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import com.example.sisvvapp.ui.utils.getDeviceType
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import com.example.sisvvapp.ui.viewmodel.SociosViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.utils.DeviceType

@Composable
fun SociosScreen(
    socios: List<SocioEntity>,
    isLoading: Boolean,
    searchQuery: String,

    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onSocioClick: (Int) -> Unit
) {

    val isTablet = getDeviceType() == DeviceType.TABLET

    VistaVerdeScaffold(
        title = stringResource(R.string.title_socios),
        onMenuClick = onMenuClick,
        showConnectionBanner = true

    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isTablet) 24.dp else 16.dp)
            .widthIn(max = if (isTablet) 700.dp else 400.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            VistaVerdeSectionHeader(text = "Buscar Socios")
            Spacer(modifier = Modifier.height(16.dp))

            VistaVerdeSearchBar(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = stringResource(R.string.title_buscar_socio)
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                SociosList(
                    socios = socios,
                    onSocioClick = { socio ->
                        onSocioClick(socio.id)
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SociosScreenPreview() {
    val mockSocios = listOf(
        SocioEntity(
            id = 1, nombre = "Cristian", apellidoP = "Meza", apellidoM = "García",
            telefono = null, email = null, firmaAutorizada = true,
            estatus = "Activo", fotoUrl = "", membresiaTipo = "Gold"
        ),
        SocioEntity(
            id = 2, nombre = "Juan", apellidoP = "Pérez", apellidoM = "López",
            telefono = null, email = null, firmaAutorizada = false,
            estatus = "Inactivo", fotoUrl = "", membresiaTipo = "Silver"
        )
    )

    SISVVAPPTheme {
        SociosScreen(
            socios = mockSocios,
            isLoading = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onMenuClick = {},
            onSocioClick = {}
        )
    }
}