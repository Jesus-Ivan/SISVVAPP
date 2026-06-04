package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import java.util.Locale

@Composable
fun BuscarProductosScreen(
    productos: List<ProductoEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    carritoCount: Int,
    onAddProducto: (ProductoEntity, Int) -> Unit,
    onProductoConModificadores: (ProductoEntity, Int) -> Unit,
    onVerCarrito: () -> Unit,
    onBackClick: () -> Unit,
    isOnline: Boolean = true
) {
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
                                    onAdd = { cantidad -> onAddProducto(producto, cantidad) },
                                    onAddConModif = { cantidad -> onProductoConModificadores(producto, cantidad) }
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
    onAdd: (Int) -> Unit,
    onAddConModif: (Int) -> Unit
) {
    var cantidad by remember { mutableStateOf(1) }

    VistaVerdeBaseCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.descripcion,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto.categoria,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(Locale.US, "%.2f", producto.precio)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Selector de cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (cantidad > 1) cantidad-- },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Menos",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$cantidad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = { cantidad++ },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Más",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                FilledTonalButton(
                    onClick = {
                        if (hasModificadores) {
                            onAddConModif(cantidad)
                        } else {
                            onAdd(cantidad)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.buscar_productos_agregar),
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
            onAddProducto = { _, _ -> },
            onProductoConModificadores = { _, _ -> },
            onVerCarrito = {},
            onBackClick = {}
        )
    }
}
