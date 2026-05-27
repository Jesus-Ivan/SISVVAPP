package com.example.sisvvapp.ui.navigation

object ScreenRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val CAJA = "caja"
    const val VENTAS = "ventas"
    const val SOCIOS = "socios"
    const val AJUSTES = "ajustes"
    const val NUEVA_VENTA = "nueva_venta"

    const val PERFIL_SOCIO = "perfil_socio/{socioId}"

    fun crearRutaPerfilSocio(socioId: Int) = "perfil_socio/$socioId"
}