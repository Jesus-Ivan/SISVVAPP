package com.sisvv.mobile.network.dto.socios

import com.google.gson.annotations.SerializedName

data class IntegranteDto(
    val id: Int?,
    val nombre: String,
    val parentesco: String,
    @SerializedName("foto_url") val fotoUrl: String
)
