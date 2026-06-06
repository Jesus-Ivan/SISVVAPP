package com.example.sisvvapp.ui.navigation

object ScreenRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val CAJA = "caja"
    const val VENTAS = "ventas"
    const val SOCIOS = "socios"
    const val AJUSTES = "ajustes"
    const val NUEVA_VENTA = "nueva_venta"
    const val BUSCAR_PRODUCTOS = "buscar_productos"
    const val MODIFICADORES = "modificadores/{productoId}"
    const val RESUMEN_CARRITO = "resumen_carrito"
    const val DETALLE_VENTA = "detalle_venta/{id}"

    const val PERFIL_SOCIO = "perfil_socio/{socioId}"

    fun crearRutaPerfilSocio(socioId: Int) = "perfil_socio/$socioId"
    fun crearRutaModificadores(productoId: Int) = "modificadores/$productoId"
    fun crearRutaDetalleVenta(id: String) = "detalle_venta/$id"
}

object NavGraphs {
    const val VENTAS_GRAPH = "ventas_graph"
    const val SOCIOS_GRAPH = "socios_graph"
    const val AJUSTES_GRAPH = "ajustes_graph"
}
