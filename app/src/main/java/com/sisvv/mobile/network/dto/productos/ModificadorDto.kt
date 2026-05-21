package com.sisvv.mobile.network.dto.productos

import com.google.gson.annotations.SerializedName

data class ModificadorDto(
    val nombre: String,
    @SerializedName("precio_extra") val precioExtra: Double,
    val tipo: String
)
