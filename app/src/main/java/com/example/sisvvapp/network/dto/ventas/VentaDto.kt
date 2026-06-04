package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName

data class VentaDto(
    val folio: Int,
    @SerializedName("nombre") val nombreCliente: String,
    val hora: String,
    val total: Double,
    val estatus: String,
    @SerializedName("caja_id")       val cajaId: Int?,
    @SerializedName("socio_id")      val socioId: Int?,
    @SerializedName("tipo_cliente")  val tipoCliente: String?,
    val fecha: String?,
    @SerializedName("clave_punto_venta") val clavePuntoVenta: String = "",
    val productos: List<ProductoVentaDto> = emptyList(),
    val pagos: List<PagoDto> = emptyList()
)
