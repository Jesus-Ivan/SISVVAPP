package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.ui.components.ResponsiveContainer
import com.example.sisvvapp.ui.components.VistaVerdeBaseCard
import com.example.sisvvapp.ui.components.VistaVerdeEmptyState
import com.example.sisvvapp.ui.components.VistaVerdeScaffold
import com.example.sisvvapp.ui.components.VistaVerdeSearchBar
import com.example.sisvvapp.ui.components.VistaVerdeSectionHeader
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
    VistaVerdeScaffold(
        title = stringResource(R.string.buscar_productos_title),
        subtitle = stringResource(R.string.buscar_productos_subtitle),
        onMenuClick = onBackClick,
        isBackButton = true,
        isOnline = isOnline
    ) {
        ResponsiveContainer {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    VistaVerdeSearchBar(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = stringResource(R.string.buscar_productos_placeholder)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    VistaVerdeSectionHeader(
                        text = stringResource(R.string.buscar_productos_title)
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
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    BadgedBox(
                        badge = {
                            if (carritoCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text("$carritoCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(R.string.buscar_productos_carrito, carritoCount),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VistaVerdeProductoCard(
    producto: ProductoEntity,
    hasModificadores: Boolean,
    onAdd: (Int, String) -> Unit,
    onAddConModif: (Int) -> Unit
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET
    val haptic = LocalHapticFeedback.current

    var cantidad by remember { mutableStateOf(1) }
    var observaciones by remember { mutableStateOf("") }
    var showObs by remember { mutableStateOf(false) }
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
                    onValueChange = { observaciones = it },
                    placeholder = { Text("Instrucciones...") },
                    modifier = Modifier.fillMaxWidth(),
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
