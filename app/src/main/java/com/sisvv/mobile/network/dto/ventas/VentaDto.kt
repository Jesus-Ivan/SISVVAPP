package com.sisvv.mobile.network.dto.ventas

import com.google.gson.annotations.SerializedName
import com.sisvv.mobile.network.dto.productos.ItemCarritoDto

data class VentaDto(
    val folio: Int,
    @SerializedName("nombre_cliente") val nombreCliente: String,
    val hora: String,
    val total: Double,
    val estatus: String,
    val items: List<ItemCarritoDto> = emptyList(),
    @SerializedName("caja_id")     val cajaId: Int?,
    @SerializedName("socio_id")    val socioId: Int?,
    @SerializedName("tipo_cliente") val tipoCliente: String?,
    val fecha: String?,
    @SerializedName("productos") val productos: List<ProductoVentaDto> = emptyList(),
    val pagos: List<PagoDto> = emptyList()
)
