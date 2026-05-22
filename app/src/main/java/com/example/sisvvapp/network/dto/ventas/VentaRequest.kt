package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto

data class VentaRequest(
    @SerializedName("tipo_venta")        val tipoVenta: String,
    @SerializedName("id_socio")          val idSocio: Int?,
    @SerializedName("clave_punto_venta") val clavePuntoVenta: Int,
    val productos: List<ItemCarritoDto>
)
