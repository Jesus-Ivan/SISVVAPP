package com.example.sisvvapp.network.dto.socios

import com.google.gson.annotations.SerializedName

data class SocioDto(
    val id: Int,
    val nombre: String,
    @SerializedName("apellido_p") val apellidoP: String,
    @SerializedName("apellido_m") val apellidoM: String,
    @SerializedName("num_accion") val numAccion: Int?,
    val firma: Boolean,
    @SerializedName("img_path")   val imgPath: String?,
    val membresia: MembresiaDto?,
    val integrantes: List<IntegranteDto> = emptyList()
)
