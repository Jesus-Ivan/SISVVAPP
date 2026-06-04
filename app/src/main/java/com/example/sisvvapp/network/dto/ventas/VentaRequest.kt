package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto

data class VentaRequest(
    @SerializedName("corte_caja")        val corteCaja: Int,
    @SerializedName("tipo_venta")        val tipoVenta: String,
    @SerializedName("id_socio")          val idSocio: Int?,
    val nombre: String?,
    @SerializedName("clave_punto_venta") val clavePuntoVenta: String,
    val productos: List<ItemCarritoDto>
)
