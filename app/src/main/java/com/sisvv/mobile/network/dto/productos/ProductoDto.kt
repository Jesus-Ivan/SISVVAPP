package com.sisvv.mobile.network.dto.productos

import com.google.gson.annotations.SerializedName

data class ProductoDto(
    val clave: Int,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    @SerializedName("print_default") val printDefault: Boolean,
    @SerializedName("tiene_modificadores") val tieneModificadores: Boolean,
    @SerializedName("imagen_url") val imagenUrl: String = ""
)
