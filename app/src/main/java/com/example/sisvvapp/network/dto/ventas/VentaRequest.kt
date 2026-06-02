package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName
import com.example.sisvvapp.network.dto.productos.ItemCarritoDto

data class PagoRequest(
    @SerializedName("id_tipo_pago") val idTipoPago: Int,
    val nombre: String,
    val monto: Double,
    val propina: Double = 0.0
)

data class VentaRequest(
    @SerializedName("corte_caja")        val corteCaja: Int,
    @SerializedName("tipo_venta")        val tipoVenta: String,
    @SerializedName("id_socio")          val idSocio: Int?,
    val nombre: String?,
    @SerializedName("clave_punto_venta") val clavePuntoVenta: String,
    val productos: List<ItemCarritoDto>,
    val pagos: List<PagoRequest>? = null
)
