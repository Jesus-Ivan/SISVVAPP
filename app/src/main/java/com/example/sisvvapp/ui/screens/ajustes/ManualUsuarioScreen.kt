package com.example.sisvvapp.ui.screens.ajustes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy

private const val MANUAL_ASSET_PATH = "manual/Manual_Usuario_App_Vista_Verde.pdf"

@Composable
fun ManualUsuarioScreen(
    onBackClick: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    VistaVerdeScaffold(
        title = "Manual de Usuario",
        onMenuClick = onBackClick,
        isBackButton = true
    ) {
        if (hasError) {
            VistaVerdeEmptyState(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                message = "No se pudo abrir el manual de usuario.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PDFView(ctx, null).apply {
                            fromAsset(MANUAL_ASSET_PATH)
                                .enableSwipe(true)
                                .pageSnap(true)
                                .pageFitPolicy(FitPolicy.WIDTH)
                                .onLoad { isLoading = false }
                                .onError { hasError = true }
                                .load()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VerdePrincipal)
                    }
                }
            }
        }
    }
}
