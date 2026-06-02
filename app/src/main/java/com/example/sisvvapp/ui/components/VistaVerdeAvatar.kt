package com.example.sisvvapp.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sisvvapp.ui.utils.ImageUtils

@Composable
fun VistaVerdeAvatar(
    fotoUrl: String? = null,
    modifier: Modifier = Modifier.size(57.dp)
) {
    val context = LocalContext.current
    val model = remember(fotoUrl) {
        val localFile = ImageUtils.getLocalPhotoFile(context, fotoUrl)
        if (localFile != null) {
            Uri.fromFile(localFile)
        } else {
            ImageUtils.sanitizarUrlFoto(fotoUrl)
        }
    }

    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(57.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(57.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}