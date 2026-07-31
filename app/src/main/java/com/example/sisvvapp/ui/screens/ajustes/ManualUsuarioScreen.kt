package com.example.sisvvapp.ui.screens.ajustes

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.theme.VerdePrincipal
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MANUAL_ASSET_PATH = "manual/Manual_Usuario_App_Vista_Verde.pdf"
private const val MANUAL_CACHE_FILE = "manual_usuario.pdf"

sealed interface ManualUiState {
    data object Loading : ManualUiState
    data class Success(val pages: List<Bitmap>) : ManualUiState
    data class Error(val message: String) : ManualUiState
}

private class ManualRendererHolder {
    var renderer: PdfRenderer? = null
    var pfd: ParcelFileDescriptor? = null
    var pages: List<Bitmap> = emptyList()

    fun close() {
        pages.forEach { if (!it.isRecycled) it.recycle() }
        pages = emptyList()
        renderer?.close()
        renderer = null
        pfd?.close()
        pfd = null
    }
}

@Composable
fun ManualUsuarioScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val holder = remember { ManualRendererHolder() }
    var uiState by remember { mutableStateOf<ManualUiState>(ManualUiState.Loading) }

    LaunchedEffect(Unit) {
        val pages = withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, MANUAL_CACHE_FILE)
                context.assets.open(MANUAL_ASSET_PATH).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                holder.renderer = renderer
                holder.pfd = pfd

                val density = context.resources.displayMetrics.density
                val widthPx = context.resources.displayMetrics.widthPixels
                val list = ArrayList<Bitmap>(renderer.pageCount)
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        val heightPx = (widthPx * page.height / page.width).coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        list.add(bitmap)
                    }
                }
                holder.pages = list
                list
            } catch (e: Exception) {
                null
            }
        }

        if (pages != null && pages.isNotEmpty()) {
            uiState = ManualUiState.Success(pages)
        } else {
            holder.close()
            uiState = ManualUiState.Error("No se pudo abrir el manual de usuario.")
        }
    }

    DisposableEffect(Unit) {
        onDispose { holder.close() }
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(count = state.pages.size) { index ->
                        PageItem(
                            bitmap = state.pages[index],
                            pageNumber = index + 1,
                            totalPages = state.pages.size
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageItem(
    bitmap: Bitmap,
    pageNumber: Int,
    totalPages: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Página $pageNumber de $totalPages",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ZoomablePage(bitmap = bitmap)
    }
}

@Composable
private fun ZoomablePage(bitmap: Bitmap) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { size ->
                containerWidth = size.width
                containerHeight = size.height
            }
            .pointerInput(Unit) {
                detectarZoomPan(isZoomed = { scale > 1f }) { centroid, pan, zoom ->
                    val newScale = (scale * zoom).coerceIn(1f, 6f)
                    val factor = newScale / scale
                    offsetX = centroid.x + (offsetX - centroid.x) * factor + pan.x
                    offsetY = centroid.y + (offsetY - centroid.y) * factor + pan.y
                    scale = newScale

                    val maxX = containerWidth * (newScale - 1f) / 2f
                    val maxY = containerHeight * (newScale - 1f) / 2f
                    offsetX = offsetX.coerceIn(-maxX, maxX)
                    offsetY = offsetY.coerceIn(-maxY, maxY)
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Página del manual de usuario",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private suspend fun PointerInputScope.detectarZoomPan(
    isZoomed: () -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)

        var pastTouchSlop = false
        var zoomAccum = 1f
        var panAccum = Offset.Zero
        val touchSlop = viewConfiguration.touchSlop

        do {
            val event = awaitPointerEvent()
            val multiTouch = event.changes.count { it.pressed } > 1

            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()

            if (!pastTouchSlop) {
                zoomAccum *= zoomChange
                panAccum += panChange
                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                val zoomMotion = abs(1 - zoomAccum) * centroidSize
                val panMotion = panAccum.getDistance()
                if (zoomMotion > touchSlop || panMotion > touchSlop) {
                    pastTouchSlop = true
                }
            }

            val shouldHandle = multiTouch || isZoomed()
            if (pastTouchSlop && shouldHandle) {
                val centroid = event.calculateCentroid(useCurrent = false)
                onGesture(centroid, panChange, zoomChange)
                event.changes.forEach { it.consume() }
            }
        } while (event.changes.any { it.pressed })
    }
}
