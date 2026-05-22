package com.example.sisvvapp.network.dto.socios

import com.google.gson.annotations.SerializedName

data class IntegranteDto(
    val id: Int?,
    val nombre: String,
    val parentesco: String,
    @SerializedName("img_path") val fotoUrl: String?
)
