package com.sisvv.mobile.network.dto.socios

import com.google.gson.annotations.SerializedName

data class MembresiaDto(
    val id: Int,
    val tipo: String,
    @SerializedName("fecha_vigencia") val fechaVigencia: String
)
