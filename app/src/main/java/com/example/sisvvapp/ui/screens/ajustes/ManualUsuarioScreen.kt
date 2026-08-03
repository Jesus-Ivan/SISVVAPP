package com.example.sisvvapp.ui.screens.ajustes

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sisvvapp.data.sync.ManualDownloader
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.launch
import java.io.File

private const val MANUAL_ASSET_PATH = "manual/Manual_Usuario_App_Vista_Verde.pdf"

@Composable
fun ManualUsuarioScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var manualPath by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val file = ManualDownloader.getManualFile(context)
        manualPath = if (file.exists()) file.absolutePath else null
        isLoading = false
    }

    fun refreshManual() {
        if (isRefreshing) return
        scope.launch {
            isRefreshing = true
            val ok = ManualDownloader.download(context)
            isRefreshing = false
            if (ok) {
                manualPath = ManualDownloader.getManualFile(context).absolutePath
                Toast.makeText(context, "Manual actualizado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se pudo descargar el manual", Toast.LENGTH_SHORT).show()
            }
        }
    }

    VistaVerdeScaffold(
        title = "Manual de Usuario",
        onMenuClick = onBackClick,
        isBackButton = true,
        actions = {
            IconButton(onClick = { refreshManual() }, enabled = !isRefreshing) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar manual")
            }
        }
    ) {
        if (hasError) {
            VistaVerdeEmptyState(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                message = "No se pudo abrir el manual de usuario.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                key(manualPath) {
                    AndroidView(
                        factory = { ctx ->
                            PDFView(ctx, null).apply {
                                val config = if (manualPath != null) {
                                    fromFile(File(manualPath!!))
                                } else {
                                    fromAsset(MANUAL_ASSET_PATH)
                                }
                                config
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
                }

                if (isLoading || isRefreshing) {
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
