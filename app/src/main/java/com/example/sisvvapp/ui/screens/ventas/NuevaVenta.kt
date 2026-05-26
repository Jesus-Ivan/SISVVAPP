package com.example.sisvvapp.ui.screens.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.theme.VerdePrincipal
import com.example.sisvvapp.ui.viewmodel.NuevaVentaStep
import com.example.sisvvapp.ui.viewmodel.NuevaVentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaVentaScreen(
    onBack: () -> Unit,
    viewModel: NuevaVentaViewModel = viewModel()
) {
    val step by viewModel.currentStep.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val productoConMods by viewModel.productoConMods.collectAsState()
    val selectedModIds by viewModel.selectedModIds.collectAsState()
    val carrito by viewModel.carrito.collectAsState()
    val socios by viewModel.socios.collectAsState()
    val socioSeleccionado by viewModel.socioSeleccionado.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var socioSearchQuery by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }

    val title = when (step) {
        NuevaVentaStep.PRODUCT_SEARCH -> "Nueva Venta"
        NuevaVentaStep.MODIFIER_SELECTION -> "Modificadores"
        NuevaVentaStep.PARTNER_SEARCH -> "Seleccionar Socio"
        NuevaVentaStep.CONFIRMATION -> "Confirmar Venta"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        when (step) {
                            NuevaVentaStep.PRODUCT_SEARCH -> onBack()
                            NuevaVentaStep.MODIFIER_SELECTION -> viewModel.volverAListaProductos()
                            NuevaVentaStep.PARTNER_SEARCH -> viewModel.volverAListaProductos()
                            NuevaVentaStep.CONFIRMATION -> viewModel.irASeleccionarSocio()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (step == NuevaVentaStep.PRODUCT_SEARCH && carrito.isNotEmpty()) {
                        TextButton(onClick = { viewModel.irASeleccionarSocio() }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("${carrito.size}")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdePrincipal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) {
        when (step) {
            NuevaVentaStep.PRODUCT_SEARCH -> ProductSearchStep(
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    viewModel.searchProductos(query)
                },
                productos = productos,
                carrito = carrito,
                onProductClick = { viewModel.seleccionarProducto(it) },
                onRemoveFromCart = { viewModel.eliminarDelCarrito(it) },
                onCartClick = { viewModel.irASeleccionarSocio() },
                onIrASocio = { viewModel.irASeleccionarSocio() }
            )

            NuevaVentaStep.MODIFIER_SELECTION -> ModifierSelectionStep(
                productoConMods = productoConMods,
                selectedModIds = selectedModIds,
                onToggleMod = { viewModel.toggleModificador(it) },
                onConfirm = { viewModel.confirmarModificadores() }
            )

            NuevaVentaStep.PARTNER_SEARCH -> PartnerSearchStep(
                searchQuery = socioSearchQuery,
                onSearchQueryChange = { query ->
                    socioSearchQuery = query
                    viewModel.searchSocios(query)
                },
                socios = socios,
                onSocioClick = { viewModel.seleccionarSocio(it) },
                onSkip = { viewModel.saltarSocio("Invitado") }
            )

            NuevaVentaStep.CONFIRMATION -> ConfirmationStep(
                carrito = carrito,
                socio = socioSeleccionado,
                onGuardar = {
                    viewModel.guardarVentaOffline {
                        showSuccessDialog = true
                    }
                }
            )
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onBack()
                }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Venta guardada") },
            text = { Text("La venta se guardó correctamente y se sincronizará cuando haya conexión.") }
        )
    }
}

// ── Step 1: Product Search ──────────────────────────────────────────────

@Composable
private fun ProductSearchStep(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    productos: List<com.example.sisvvapp.data.local.entity.ProductoEntity>,
    carrito: List<com.example.sisvvapp.network.dto.productos.ItemCarritoDto>,
    onProductClick: (Int) -> Unit,
    onRemoveFromCart: (Int) -> Unit,
    onCartClick: () -> Unit,
    onIrASocio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar producto...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Productos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (carrito.isNotEmpty()) {
                Text(
                    text = "Carrito: ${carrito.size} artículos",
                    style = MaterialTheme.typography.bodySmall,
                    color = VerdePrincipal
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productos, key = { it.id }) { producto ->
                ProductoCard(
                    descripcion = producto.descripcion,
                    precio = producto.precio,
                    onClick = { onProductClick(producto.id) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (carrito.isNotEmpty()) {
            CartPreview(
                carrito = carrito,
                onRemove = onRemoveFromCart,
                onContinue = onIrASocio
            )
        }
    }
}

@Composable
private fun ProductoCard(
    descripcion: String,
    precio: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = descripcion,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$${String.format("%.2f", precio)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = VerdePrincipal
            )
        }
    }
}

@Composable
private fun CartPreview(
    carrito: List<com.example.sisvvapp.network.dto.productos.ItemCarritoDto>,
    onRemove: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdePrincipal.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Carrito actual", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            carrito.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${item.claveProducto}",
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", 0.0)}",
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = { onRemove(index) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text("Continuar (${carrito.size} artículos)")
            }
        }
    }
}

// ── Step 2: Modifier Selection ──────────────────────────────────────────

@Composable
private fun ModifierSelectionStep(
    productoConMods: com.example.sisvvapp.data.local.dao.ProductoConModificadores?,
    selectedModIds: Set<Int>,
    onToggleMod: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    val producto = productoConMods?.producto
    val modificadores = productoConMods?.modificadores ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = producto?.descripcion ?: "",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$${String.format("%.2f", producto?.precio ?: 0.0)}",
            style = MaterialTheme.typography.titleLarge,
            color = VerdePrincipal
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Selecciona modificadores",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Group modifiers by group name
        val groupedMods = modificadores.groupBy { it.grupo }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedMods.forEach { (grupo, mods) ->
                item {
                    Text(
                        text = grupo,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(mods, key = { it.id }) { mod ->
                    val isSelected = mod.id in selectedModIds
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleMod(mod.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                VerdePrincipal.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            androidx.compose.foundation.BorderStroke(2.dp, VerdePrincipal)
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mod.nombre,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (mod.incluido) {
                                    Text(
                                        text = "Incluido",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VerdePrincipal
                                    )
                                }
                            }
                            if (mod.precio > 0) {
                                Text(
                                    text = "+$${String.format("%.2f", mod.precio)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (mod.incluido) VerdePrincipal else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar al carrito")
        }
    }
}

// ── Step 3: Partner Search ──────────────────────────────────────────────

@Composable
private fun PartnerSearchStep(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    socios: List<SocioEntity>,
    onSocioClick: (SocioEntity) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar socio por nombre...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Vender como invitado")
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(socios, key = { it.id }) { socio ->
                SocioCard(
                    socio = socio,
                    onClick = { onSocioClick(socio) }
                )
            }
        }
    }
}

@Composable
private fun SocioCard(
    socio: SocioEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VerdePrincipal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = VerdePrincipal
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${socio.nombre} ${socio.apellidoP} ${socio.apellidoM ?: ""}".trimEnd(),
                    fontWeight = FontWeight.Medium
                )
                if (!socio.telefono.isNullOrBlank()) {
                    Text(
                        text = socio.telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Step 4: Confirmation ────────────────────────────────────────────────

@Composable
private fun ConfirmationStep(
    carrito: List<com.example.sisvvapp.network.dto.productos.ItemCarritoDto>,
    socio: SocioEntity?,
    onGuardar: () -> Unit
) {
    val total = carrito.sumOf { it.cantidad * 0.0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen de venta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))

                    if (socio != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cliente", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${socio.nombre} ${socio.apellidoP}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cliente", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Invitado", fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Text("Productos:", fontWeight = FontWeight.Medium)
                    carrito.forEachIndexed { index, item ->
                        Text(
                            text = "  ${index + 1}. Producto #${item.claveProducto} x${item.cantidad}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = VerdePrincipal
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar venta", fontSize = 16.sp)
            }
        }
    }
}
