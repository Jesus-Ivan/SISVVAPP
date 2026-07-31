package com.example.sisvvapp.ui.screens.ajustes

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.pdf.PdfDocument
import androidx.pdf.SandboxedPdfLoader
import androidx.pdf.compose.PdfViewer
import androidx.pdf.compose.PdfViewerState
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.theme.VerdePrincipal
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MANUAL_ASSET_PATH = "manual/Manual_Usuario_App_Vista_Verde.pdf"
private const val MANUAL_CACHE_FILE = "manual_usuario.pdf"

sealed interface ManualUiState {
    data object Loading : ManualUiState
    data class Success(val document: PdfDocument) : ManualUiState
    data class Error(val message: String) : ManualUiState
}

@Composable
fun ManualUsuarioScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val loader = remember { SandboxedPdfLoader(context) }
    val viewerState = remember { PdfViewerState() }
    var uiState by remember { mutableStateOf<ManualUiState>(ManualUiState.Loading) }

    LaunchedEffect(Unit) {
        val document = withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, MANUAL_CACHE_FILE)
                context.assets.open(MANUAL_ASSET_PATH).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                val uri = Uri.fromFile(file)
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                loader.openDocument(uri, pfd)
            } catch (e: Exception) {
                null
            }
        }

        if (document != null) {
            uiState = ManualUiState.Success(document)
        } else {
            uiState = ManualUiState.Error("No se pudo abrir el manual de usuario.")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            (uiState as? ManualUiState.Success)?.document?.close()
        }
    }

    VistaVerdeScaffold(
        title = "Manual de Usuario",
        onMenuClick = onBackClick,
        isBackButton = true
    ) {
        when (val state = uiState) {
            is ManualUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VerdePrincipal)
                }
            }

            is ManualUiState.Error -> {
                VistaVerdeEmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    message = state.message,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is ManualUiState.Success -> {
                PdfViewer(
                    pdfDocument = state.document,
                    state = viewerState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
