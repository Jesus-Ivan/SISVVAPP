# Plan: Quitar snackbar de reimpresión en DetalleVentaScreen

## Objetivo
Eliminar el snackbar que aparece en el detalle de venta cuando hay productos en estado COLA/ERROR, junto con su botón "REIMPRIMIR". No se modifican los colores de los badges de producto.

## Archivos a modificar

### 1. `app/src/main/java/com/example/sisvvapp/ui/screens/ventas/DetalleVentaScreen.kt`

#### 1a. Firma de la función
```kotlin
// ANTES:
fun DetalleVentaScreen(
    venta: VentaDto?,
    isLoading: Boolean,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onAgregarProductos: () -> Unit,
    onReimprimir: () -> Unit = {},   // ← ELIMINAR
    onTransferirProducto: ((ProductoVentaDto) -> Unit)? = null
)

// DESPUÉS:
fun DetalleVentaScreen(
    venta: VentaDto?,
    isLoading: Boolean,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onAgregarProductos: () -> Unit,
    onTransferirProducto: ((ProductoVentaDto) -> Unit)? = null
)
```

#### 1b. Variables y LaunchedEffect (líneas 40-58)
Eliminar:
- `val snackbarHostState = remember { SnackbarHostState() }`
- `val snackbarShown = remember { mutableStateOf(false) }`
- `val tienePendientes = ...`
- Todo el bloque `LaunchedEffect(tienePendientes, venta, isOnline) { ... }`

#### 1c. SnackbarHost (líneas 180-232)
Eliminar todo el bloque `SnackbarHost(hostState = snackbarHostState, ...) { ... }`

#### 1d. Imports (opcional, el compilador no falla)
Se pueden dejar, pero si se quiere limpiar:
- Eliminar `import androidx.compose.material3.SnackbarHost`
- Eliminar `import androidx.compose.material3.SnackbarHostState`
- Eliminar `import androidx.compose.material3.SnackbarDuration`
- Eliminar `import androidx.compose.material3.SnackbarResult`

### 2. `app/src/main/java/com/example/sisvvapp/ui/screens/main/MainContainer.kt`

#### 2a. Llamada a DetalleVentaScreen (líneas 392-406)
Eliminar el bloque `onReimprimir = { ... },` de la llamada a `DetalleVentaScreen()`.

#### 2b. Imports (opcional)
Si `ventasViewModel.reimprimirComanda` ya no se usa en este ámbito, el import puede quedar.

## Resumen de líneas eliminadas
| Archivo | Líneas |
|---------|--------|
| `DetalleVentaScreen.kt` | ~12 líneas de código + ~52 líneas de SnackbarHost |
| `MainContainer.kt` | ~14 líneas (onReimprimir = { ... }) |

## No se modifica
- `ProductoDetalleCard` — colores según `idEstado` se mantienen
- `VentasComponents.kt` — sin cambios
- `VentaRepository.kt`, `CarritoViewModel.kt` — sin cambios
- Lógica de impresión/reenvío — intacta
