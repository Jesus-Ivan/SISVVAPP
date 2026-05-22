package com.example.sisvvapp.ui.screens.socios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvvapp.ui.components.SociosList
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import com.example.sisvvapp.ui.viewmodel.SociosViewModel

@Composable
fun SociosScreen(
    onMenuClick: () -> Unit,
    onSocioClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: SociosViewModel = viewModel(
        factory = SisvvViewModelFactory(context)
    )

    val socios by viewModel.socios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()


    VistaVerdeScaffold(
        title = "Socios",
        onMenuClick = onMenuClick,
        showConnectionBanner = true
    ) {
        Column {
            VistaVerdeSearchBar(
                value = "",
                onValueChange = { query ->
                    viewModel.search(query)
                },
                placeholder = "Buscar socio..."
            )


            if (isLoading && socios.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
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