package com.sisvv.mobile.network.dto.productos

import com.google.gson.annotations.SerializedName

data class ModificadorSyncDto(
    val id: Int,
    @SerializedName("producto_id") val productoId: Int,
    val nombre: String,
    val tipo: String,
    val precio: Double,
    val grupo: String,
    val incluido: Boolean
)
