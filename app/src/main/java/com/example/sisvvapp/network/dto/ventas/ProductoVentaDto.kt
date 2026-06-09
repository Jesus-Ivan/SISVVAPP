package com.example.sisvvapp.network.dto.ventas

import com.example.sisvvapp.network.dto.productos.ModificadorSeleccionadoDto
import com.google.gson.annotations.SerializedName

data class ProductoVentaDto(
    val id: Int,
    @SerializedName("clave_producto") val claveProducto: Int,
    val nombre: String,
    val precio: Double,
    val cantidad: Int,
    val chunk: Long,
    val observaciones: String = "",
    val subtotal: Double = 0.0,
    val modificadores: List<ModificadorSeleccionadoDto> = emptyList()
)
