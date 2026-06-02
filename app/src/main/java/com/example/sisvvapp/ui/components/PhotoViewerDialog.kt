package com.example.sisvvapp.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sisvvapp.ui.utils.ImageUtils
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

@Composable
fun PhotoViewerOverlay(
    fotoUrl: String?,
    hazeState: HazeState,
    onDismiss: () -> Unit
) {
    if (fotoUrl.isNullOrBlank()) return

    val context = LocalContext.current
    val model = remember(fotoUrl) {
        val localFile = ImageUtils.getLocalPhotoFile(context, fotoUrl)
        if (localFile != null) {
            Uri.fromFile(localFile)
        } else {
            ImageUtils.sanitizarUrlFoto(fotoUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.5f))
            .hazeChild(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.White.copy(alpha = 0.5f),
                    tint = null,
                    blurRadius = 20.dp
                )
            )
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                // Usamos statusBarsPadding para que no se pegue arriba del todo
                // y añadimos 24.dp o 32.dp extras para bajarlo a tu gusto
                .statusBarsPadding()
                .padding(top = 24.dp, end = 24.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cerrar",
                tint = Color.White, // Blanco resalta mejor sobre el fondo oscuro estilo IG
                modifier = Modifier.size(28.dp)
            )
        }

        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Foto ampliada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    // Evita que el clic en la foto cierre el overlay inesperadamente
                    .clickable(enabled = false) {}
            )
        }
    }
}
