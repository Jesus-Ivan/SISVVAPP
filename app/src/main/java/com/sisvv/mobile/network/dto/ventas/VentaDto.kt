package com.sisvv.mobile.network.dto.ventas

import com.google.gson.annotations.SerializedName
import com.sisvv.mobile.network.dto.productos.ItemCarritoDto

data class VentaDto(
    val folio: Int,
    @SerializedName("nombre_cliente") val nombreCliente: String,
    val hora: String,
    val total: Double,
    val estatus: String,
    val items: List<ItemCarritoDto> = emptyList()
)
