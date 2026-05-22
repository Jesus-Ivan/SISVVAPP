package com.sisvv.mobile.network.dto.cajas

import com.google.gson.annotations.SerializedName

data class CajaDto(
    val id: Int,
    val nombre: String,
    @SerializedName("fecha_apertura") val fechaApertura: String,
    @SerializedName("fecha_cierre")   val fechaCierre: String?,
    val activo: Boolean,
    @SerializedName("mesero_id")      val meseroId: Int?
)
