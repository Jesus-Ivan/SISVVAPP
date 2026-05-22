package com.sisvv.mobile.network.dto.ventas

import com.google.gson.annotations.SerializedName

data class PagoDto(
    val id: Int,
    @SerializedName("tipo_pago_id") val tipoPagoId: Int,
    val monto: Double,
    val fecha: String
)
