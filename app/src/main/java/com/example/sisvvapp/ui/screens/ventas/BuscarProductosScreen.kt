package com.example.sisvvapp.ui.screens.ventas

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.utils.ImageUtils
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import com.example.sisvvapp.ui.theme.Grey950
import com.example.sisvvapp.ui.theme.OrangeSaaS
import com.example.sisvvapp.ui.theme.Poppins
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import java.util.Locale
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun BuscarProductosScreen(
    productos: List<ProductoEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    carritoCount: Int,
    onAddProducto: (ProductoEntity, Int, String) -> Unit,
    onProductoConModificadores: (ProductoEntity, Int) -> Unit,
    onVerCarrito: () -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val hazeState = remember { HazeState() }
    var productoImg by remember { mutableStateOf<ProductoEntity?>(null) }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(Unit) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling) keyboardController?.hide()
            }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().haze(hazeState)) {
            VistaVerdeScaffold(
                title = stringResource(R.string.buscar_productos_title),
                subtitle = stringResource(R.string.buscar_productos_subtitle),
                onMenuClick = onBackClick,
                isBackButton = true,
                isOnline = isOnline
            ) {
                ResponsiveContainer {
                    Box(modifier = Modifier.fillMaxSize().imePadding()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    var searchFieldValue by remember { mutableStateOf(TextFieldValue(searchQuery, TextRange(searchQuery.length))) }
                    LaunchedEffect(searchQuery) {
                        if (searchQuery != searchFieldValue.text) {
                            searchFieldValue = TextFieldValue(searchQuery, TextRange(searchQuery.length))
                        }
                    }
                    VistaVerdeSearchBar(
                        value = searchFieldValue,
                        onValueChange = { newValue ->
                            searchFieldValue = newValue
                            onSearchQueryChange(newValue.text)
                        },
                        placeholder = stringResource(R.string.buscar_productos_placeholder),
                        modifier = Modifier.focusRequester(focusRequester)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (productos.isEmpty()) {
                        VistaVerdeEmptyState(
                            icon = Icons.Default.ShoppingCart,
                            message = stringResource(R.string.buscar_productos_empty),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            state = listState,
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = productos,
                                key = { it.id }
                            ) { producto ->
                                VistaVerdeProductoCard(
                                    producto = producto,
                                    hasModificadores = producto.modifMaximos > 0,
                                    onImageClick = { productoImg = it },
                                    onAdd = { cantidad, obs ->
                                        onSearchQueryChange("")
                                        keyboardController?.hide()
                                        onAddProducto(producto, cantidad, obs)
                                    },
                                    onAddConModif = { cantidad ->
                                        onProductoConModificadores(producto, cantidad)
                                    }
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = onVerCarrito,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .size(80.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (carritoCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                                ) {
                                    Text(
                                        text = carritoCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(
                                R.string.buscar_productos_carrito,
                                carritoCount
                            ),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    }
                }
                }
            }
        }

                productoImg?.let { p ->
                    ProductoImagenOverlay(
                        producto = p,
                        hazeState = hazeState,
                        onDismiss = { productoImg = null }
                    )
                }
    }
}

@Composable
private fun VistaVerdeProductoCard(
    producto: ProductoEntity,
    hasModificadores: Boolean,
    onImageClick: (ProductoEntity) -> Unit,
    onAdd: (Int, String) -> Unit,
    onAddConModif: (Int) -> Unit
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET
    val haptic = LocalHapticFeedback.current

    var cantidad by remember { mutableStateOf(1) }
    var observaciones by remember { mutableStateOf("") }
    var showObs by remember { mutableStateOf(false) }
    val obsFocusRequester = remember { FocusRequester() }
    LaunchedEffect(showObs) { if (showObs) obsFocusRequester.requestFocus() }
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val borderAlpha = remember { Animatable(0f) }

    VistaVerdeBaseCard {
        Column(
            modifier = Modifier
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                .drawBehind {
                    if (borderAlpha.value > 0f) {
                        drawRoundRect(
                            color = Color(0xFF4CAF50).copy(alpha = borderAlpha.value * 0.18f),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    }
                }
                .padding(if (isTablet) 20.dp else 16.dp)
        ) {
            // Fila 1: Información y Precio (Balanceado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.descripcion.uppercase(),
                        style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "$${String.format(Locale.US, "%.2f", producto.precio)}",
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (showObs) {
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = observaciones,
                    onValueChange = { observaciones = it.take(250) },
                    placeholder = { Text("Instrucciones...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(obsFocusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fila 2: Acciones refinadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!producto.imagenUrl.isNullOrBlank()) {
                        Surface(
                            onClick = { onImageClick(producto) },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(if (isTablet) 44.dp else 36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    if (!hasModificadores) {
                        Surface(
                            onClick = { showObs = !showObs },
                            color = if (showObs) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(if (isTablet) 44.dp else 36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = if (showObs) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                            .padding(horizontal = 2.dp)
                    ) {
                        IconButton(onClick = { if (cantidad > 1) cantidad-- }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "$cantidad",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { cantidad++ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (hasModificadores) onAddConModif(cantidad)
                        else {
                            scope.launch {
                                coroutineScope {
                                    launch { scale.animateTo(1.08f, tween(120)) }
                                    launch { borderAlpha.animateTo(1f, tween(120)) }
                                }
                                coroutineScope {
                                    launch { scale.animateTo(1f, tween(300)) }
                                    launch { borderAlpha.animateTo(0f, tween(300)) }
                                }
                                onAdd(cantidad, observaciones)
                                observaciones = ""
                                showObs = false
                            }
                        }
                    },
                    modifier = Modifier.height(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text(
                        text = if (hasModificadores) "CONFIGURAR" else "AGREGAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoImagenOverlay(
    producto: ProductoEntity,
    hazeState: HazeState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val model = remember(producto.imagenUrl) {
        val url = producto.imagenUrl ?: return@remember null
        val localFile = ImageUtils.getLocalPhotoFile(context, url)
        if (localFile != null) Uri.fromFile(localFile) else ImageUtils.sanitizarUrlFoto(url)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .hazeChild(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = 0.7f),
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
                .statusBarsPadding()
                .padding(top = 24.dp, end = 24.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        if (model != null) {
            ZoomableImage(
                model = model,
                contentDescription = producto.descripcion,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.72f)
                    .padding(horizontal = 24.dp)
            )
            Text(
                text = producto.descripcion.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
            )
        } else {
            VistaVerdeEmptyState(
                icon = Icons.Default.Image,
                message = "Imagen no disponible",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ZoomableImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val cornerRadiusPx = with(LocalDensity.current) { 20.dp.toPx() }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 6f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (model != null) {
            val imageRequest = remember(model) {
                ImageRequest.Builder(context)
                    .data(model)
                    .transformations(RoundedCornersTransformation(cornerRadiusPx))
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BuscarProductosScreenPreview() {
    SISVVAPPTheme {
        BuscarProductosScreen(
            productos = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            carritoCount = 0,
            onAddProducto = { _, _, _ -> },
            onProductoConModificadores = { _, _ -> },
            onVerCarrito = {},
            onBackClick = {}
        )
    }
}
