package com.example.sisvvapp.data.mock

// ─── Data Models ─────────────────────────────────────────────────────────────

data class Socio(
    val id: Int,
    val nombre: String,
    val apellidoP: String,
    val apellidoM: String,
    val numAccion: Int,
    val firma: Boolean,
    val estatus: String,   // "Activo", "Inactivo", "Cancelado"
    val fotoUrl: String
)

data class Integrante(
    val nombre: String,
    val parentesco: String,
    val fotoUrl: String
)

data class Producto(
    val clave: Int,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val printDefault: Boolean,
    val tieneModificadores: Boolean,
    val imagenUrl: String = ""
)

data class Modificador(
    val nombre: String,
    val precioExtra: Double,
    val tipo: TipoModificador  // UNICO (radio) o MULTIPLE (checkbox)
)

enum class TipoModificador { UNICO, MULTIPLE }

data class GrupoModificador(
    val titulo: String,
    val requerido: Boolean,
    val tipo: TipoModificador,
    val opciones: List<Modificador>
)

data class ItemCarrito(
    val producto: Producto,
    val modificadoresSeleccionados: List<Modificador> = emptyList(),
    val cantidad: Int = 1
) {
    val totalItem: Double
        get() = (producto.precio + modificadoresSeleccionados.sumOf { it.precioExtra }) * cantidad
}

data class Venta(
    val folio: Int,
    val nombreCliente: String,
    val hora: String,
    val total: Double,
    val estatus: String,   // "Abierta", "Cerrada", "Cancelada"
    val items: List<ItemCarrito> = emptyList()
)

data class CajaCorte(
    val id: Int,
    val nombre: String
)

data class VentaPendiente(
    val folio: Int,
    val nombreCliente: String,
    val total: Double,
    val razon: String
)

// ─── Mock Data ────────────────────────────────────────────────────────────────

val mockSocios = listOf(
    Socio(
        id = 101, nombre = "Alejandro", apellidoP = "Meza", apellidoM = "Gómez",
        numAccion = 1254, firma = true, estatus = "Activo",
        fotoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&q=80"
    ),
    Socio(
        id = 102, nombre = "María José", apellidoP = "Guzmán", apellidoM = "Díaz",
        numAccion = 895, firma = false, estatus = "Activo",
        fotoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80"
    ),
    Socio(
        id = 103, nombre = "Roberto", apellidoP = "Carlos", apellidoM = "Pérez",
        numAccion = 412, firma = true, estatus = "Inactivo",
        fotoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80"
    ),
    Socio(
        id = 104, nombre = "Claudia", apellidoP = "Ramírez", apellidoM = "Torres",
        numAccion = 678, firma = true, estatus = "Activo",
        fotoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&q=80"
    ),
    Socio(
        id = 105, nombre = "Fernando", apellidoP = "Hernández", apellidoM = "Vega",
        numAccion = 330, firma = false, estatus = "Cancelado",
        fotoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80"
    ),
    Socio(
        id = 106, nombre = "Lucía", apellidoP = "Morales", apellidoM = "Fuentes",
        numAccion = 987, firma = true, estatus = "Activo",
        fotoUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&q=80"
    )
)

val mockIntegrantesPorSocio: Map<Int, List<Integrante>> = mapOf(
    101 to listOf(
        Integrante(
            nombre = "Sofía Meza Guzmán", parentesco = "Hijo/a",
            fotoUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&q=80"
        ),
        Integrante(
            nombre = "Gabriela Gómez", parentesco = "Esposo/a",
            fotoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&q=80"
        )
    ),
    102 to listOf(
        Integrante(
            nombre = "Carlos Guzmán", parentesco = "Esposo/a",
            fotoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80"
        )
    ),
    103 to listOf(
        Integrante(
            nombre = "Ana Pérez", parentesco = "Hijo/a",
            fotoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80"
        ),
        Integrante(
            nombre = "Luis Carlos", parentesco = "Hijo/a",
            fotoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80"
        )
    )
)

val mockProductos = listOf(
    Producto(
        clave = 201, descripcion = "Rib Eye 400g", precio = 480.0,
        categoria = "Cocina", printDefault = true, tieneModificadores = true,
        imagenUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=400&q=80"
    ),
    Producto(
        clave = 202, descripcion = "Cerveza Corona", precio = 55.0,
        categoria = "Bebidas", printDefault = true, tieneModificadores = false,
        imagenUrl = "https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&q=80"
    ),
    Producto(
        clave = 203, descripcion = "Limonada", precio = 45.0,
        categoria = "Bebidas", printDefault = false, tieneModificadores = false,
        imagenUrl = "https://images.unsplash.com/photo-1621263764928-df1444c5e859?w=400&q=80"
    ),
    Producto(
        clave = 204, descripcion = "Hamburguesa Vista Verde", precio = 185.0,
        categoria = "Cocina", printDefault = true, tieneModificadores = true,
        imagenUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80"
    ),
    Producto(
        clave = 205, descripcion = "Pastel de Chocolate", precio = 95.0,
        categoria = "Postres", printDefault = true, tieneModificadores = false,
        imagenUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80"
    ),
    Producto(
        clave = 206, descripcion = "Agua Mineral", precio = 35.0,
        categoria = "Bebidas", printDefault = false, tieneModificadores = false,
        imagenUrl = "https://images.unsplash.com/photo-1559839914-17aae19cec71?w=400&q=80"
    ),
    Producto(
        clave = 207, descripcion = "Ensalada César", precio = 120.0,
        categoria = "Cocina", printDefault = true, tieneModificadores = false,
        imagenUrl = "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400&q=80"
    ),
    Producto(
        clave = 208, descripcion = "Cheesecake Frutos Rojos", precio = 85.0,
        categoria = "Postres", printDefault = true, tieneModificadores = false,
        imagenUrl = "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=400&q=80"
    )
)

val mockModificadoresRibEye = listOf(
    GrupoModificador(
        titulo = "Término de Carne",
        requerido = true,
        tipo = TipoModificador.UNICO,
        opciones = listOf(
            Modificador("Término Medio", 0.0, TipoModificador.UNICO),
            Modificador("3/4", 0.0, TipoModificador.UNICO),
            Modificador("Bien Cocido", 0.0, TipoModificador.UNICO)
        )
    ),
    GrupoModificador(
        titulo = "Ingredientes Extra",
        requerido = false,
        tipo = TipoModificador.MULTIPLE,
        opciones = listOf(
            Modificador("Queso Extra", 15.0, TipoModificador.MULTIPLE),
            Modificador("Tocino", 20.0, TipoModificador.MULTIPLE)
        )
    ),
    GrupoModificador(
        titulo = "Guarnición",
        requerido = true,
        tipo = TipoModificador.UNICO,
        opciones = listOf(
            Modificador("Papas a la francesa", 0.0, TipoModificador.UNICO),
            Modificador("Ensalada", 0.0, TipoModificador.UNICO)
        )
    )
)

val mockModificadoresHamburguesa = listOf(
    GrupoModificador(
        titulo = "Término de Carne",
        requerido = true,
        tipo = TipoModificador.UNICO,
        opciones = listOf(
            Modificador("Término Medio", 0.0, TipoModificador.UNICO),
            Modificador("3/4", 0.0, TipoModificador.UNICO),
            Modificador("Bien Cocido", 0.0, TipoModificador.UNICO)
        )
    ),
    GrupoModificador(
        titulo = "Extras",
        requerido = false,
        tipo = TipoModificador.MULTIPLE,
        opciones = listOf(
            Modificador("Queso Extra", 15.0, TipoModificador.MULTIPLE),
            Modificador("Tocino", 20.0, TipoModificador.MULTIPLE),
            Modificador("Aguacate", 25.0, TipoModificador.MULTIPLE)
        )
    ),
    GrupoModificador(
        titulo = "Guarnición",
        requerido = true,
        tipo = TipoModificador.UNICO,
        opciones = listOf(
            Modificador("Papas a la francesa", 0.0, TipoModificador.UNICO),
            Modificador("Ensalada", 0.0, TipoModificador.UNICO),
            Modificador("Aros de Cebolla", 10.0, TipoModificador.UNICO)
        )
    )
)

fun getModificadoresParaProducto(clave: Int): List<GrupoModificador> = when (clave) {
    201 -> mockModificadoresRibEye
    204 -> mockModificadoresHamburguesa
    else -> emptyList()
}

val mockVentas = mutableListOf(
    Venta(folio = 1024, nombreCliente = "Alejandro Meza", hora = "10:32", total = 535.0, estatus = "Abierta"),
    Venta(folio = 1025, nombreCliente = "Invitado Mesa 3", hora = "11:05", total = 240.0, estatus = "Abierta"),
    Venta(folio = 1026, nombreCliente = "María José Guzmán", hora = "11:48", total = 95.0, estatus = "Abierta"),
    Venta(folio = 1027, nombreCliente = "Roberto Carlos", hora = "12:15", total = 670.0, estatus = "Abierta")
)

val mockCajas = listOf(
    CajaCorte(1, "Caja Principal Bar - Turno Matutino"),
    CajaCorte(2, "Caja Restaurante - Turno Vespertino"),
    CajaCorte(3, "Caja Terraza Golf - Turno Matutino")
)

val mockVentasPendientes = listOf(
    VentaPendiente(folio = 1018, nombreCliente = "Carlos Fuentes", total = 320.0, razon = "Sin conexión a internet"),
    VentaPendiente(folio = 1019, nombreCliente = "Invitado Mesa 7", total = 185.0, razon = "Error de servidor"),
    VentaPendiente(folio = 1021, nombreCliente = "Lucía Morales", total = 560.0, razon = "Sin conexión a internet")
)
