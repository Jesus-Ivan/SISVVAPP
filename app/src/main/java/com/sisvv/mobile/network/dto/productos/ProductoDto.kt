package com.sisvv.mobile.network.dto.productos

import com.google.gson.annotations.SerializedName

data class ProductoDto(
    val id: Int = 0,
    val clave: Int,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    @SerializedName("print_default") val printDefault: Boolean = false,
    @SerializedName("tiene_modificadores") val tieneModificadores: Boolean = false,
    @SerializedName("imagen_url") val imagenUrl: String = "",
    @SerializedName("forzar_captura") val forzarCaptura: Boolean = false,
    @SerializedName("modif_incluidos") val modifIncluidos: Int = 0,
    @SerializedName("modif_maximos") val modifMaximos: Int = 0,
    @SerializedName("modificadores") val modificadores: List<ModificadorSyncDto> = emptyList()
)
