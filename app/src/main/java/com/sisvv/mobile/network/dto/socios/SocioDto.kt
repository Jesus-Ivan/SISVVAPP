package com.sisvv.mobile.network.dto.socios

import com.google.gson.annotations.SerializedName

data class SocioDto(
    val id: Int,
    val nombre: String,
    @SerializedName("apellido_p") val apellidoP: String,
    @SerializedName("apellido_m") val apellidoM: String,
    @SerializedName("num_accion") val numAccion: Int,
    val firma: Boolean,
    val estatus: String,
    @SerializedName("foto_url") val fotoUrl: String,
    val telefono: String?,
    val email: String?,
    @SerializedName("membresia") val membresia: MembresiaDto?,
    @SerializedName("integrantes") val integrantes: List<IntegranteDto> = emptyList()
)
