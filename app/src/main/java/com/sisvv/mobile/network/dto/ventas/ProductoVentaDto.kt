package com.sisvv.mobile.network.dto.ventas

import com.google.gson.annotations.SerializedName

data class ProductoVentaDto(
    val id: Int,
    @SerializedName("producto_id") val productoId: Int,
    val nombre: String,
    val precio: Double,
    val cantidad: Int,
    val chunk: Long,
    @SerializedName("modificadores") val modificadores: List<ProductoVentaModificadorDto> = emptyList()
)

data class ProductoVentaModificadorDto(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val grupo: String,
    val incluido: Boolean,
    val chunk: Long
)
