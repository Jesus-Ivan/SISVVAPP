package com.example.sisvvapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sisvvapp.ui.theme.FondoInputClaro
import com.example.sisvvapp.ui.theme.FondoInputOscuro
import com.example.sisvvapp.ui.utils.ImageUtils

@Composable
fun VistaVerdeAvatar(
    fotoUrl: String? = null,
    modifier: Modifier = Modifier.size(57.dp)
) {
    val urlFinal = remember(fotoUrl) { ImageUtils.sanitizarUrlFoto(fotoUrl) }

    if (urlFinal != null) {
        AsyncImage(
            model = urlFinal,
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
                .background(FondoInputClaro),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Avatar",
                tint = FondoInputOscuro,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}