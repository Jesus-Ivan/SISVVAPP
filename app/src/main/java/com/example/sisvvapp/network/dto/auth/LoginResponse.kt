package com.example.sisvvapp.network.dto.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val name: String,
    val permisos: List<PermisoPuntoVenta> = emptyList()
)

data class PermisoPuntoVenta(
    @SerializedName("clave_punto_venta") val clavePuntoVenta: String,
    @SerializedName("punto_venta_nombre") val puntoVentaNombre: String?,
    @SerializedName("clave_rol") val claveRol: String?
)
