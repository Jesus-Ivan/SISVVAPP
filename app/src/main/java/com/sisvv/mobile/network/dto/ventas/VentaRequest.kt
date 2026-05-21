package com.sisvv.mobile.network.dto.ventas

import com.google.gson.annotations.SerializedName
import com.sisvv.mobile.network.dto.productos.ItemCarritoDto

data class VentaRequest(
    @SerializedName("nombre_cliente") val nombreCliente: String,
    val items: List<ItemCarritoDto>,
    val total: Double
)
