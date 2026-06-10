package com.example.sisvvapp.network.dto.productos

import com.google.gson.annotations.SerializedName

/** Representa un modificador seleccionado dentro de un ítem de venta (carrito). */
data class ModificadorSeleccionadoDto(
    @SerializedName("clave_producto") val claveProducto: Int,
    val cantidad: Int,
    val precio: Double?,
    val nombre: String? = null,
    val observaciones: String = ""
)

/** Representa un ítem en el carrito de una nueva venta o al agregar productos. */
data class ItemCarritoDto(
    @SerializedName("clave_producto") val claveProducto: Int,
    val cantidad: Int,
    val observaciones: String = "",
    val modificadores: List<ModificadorSeleccionadoDto> = emptyList(),
    val nombre: String? = null, // Usado para visualización offline
    val precio: Double? = null // Usado para visualización offline
)
