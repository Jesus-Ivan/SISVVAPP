package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName

data class TipoPagoDto(
    val id: Int,
    val nombre: String,
    @SerializedName("requiere_socio") val requiereSocio: Boolean = false,
    @SerializedName("requiere_firma") val requiereFirma: Boolean = false,
    val activo: Boolean = true
)
