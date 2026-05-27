package com.example.sisvvapp.network.dto.socios

import com.google.gson.annotations.SerializedName

data class IntegranteDto(
    val id: Int?,
    val nombre: String?,
    @SerializedName("apellido_p") val apellidoP: String?,
    @SerializedName("apellido_m") val apellidoM: String?,
    val parentesco: String?,
    @SerializedName("img_path") val fotoUrl: String?
)
