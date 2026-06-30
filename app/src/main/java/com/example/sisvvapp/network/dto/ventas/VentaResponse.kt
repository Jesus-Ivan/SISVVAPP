package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName

data class VentaResponse(
    val folio: Int,
    @SerializedName("tipo_venta")     val tipoVenta: String? = null,
    @SerializedName("id_socio")       val idSocio: Int? = null,
    val nombre: String? = null,
    @SerializedName("fecha_apertura") val fechaApertura: String? = null,
    @SerializedName("fecha_cierre")   val fechaCierre: String? = null,
    val total: Any? = null,
    @SerializedName("corte_caja")     val corteCaja: Int? = null,
    @SerializedName("clave_punto_venta") val clavePuntoVenta: String? = null,
    val productos: List<ProductoVentaDto>? = null,
    val pagos: List<PagoDto>? = null,
    @SerializedName("num_comensales") val numComensales: String? = null
) {
    fun toVentaDto(): VentaDto {
        val (date, time) = fechaApertura?.split(" ")?.let {
            Pair(it.getOrElse(0) { "" }, it.getOrElse(1) { "" }.take(5))
        } ?: Pair("", "")

        val totalValue = when (total) {
            is Number -> (total as Number).toDouble()
            is String -> (total as String).toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        val estatus = if (fechaCierre.isNullOrBlank()) "Abierta" else "Cerrada"

        return VentaDto(
            folio = folio,
            nombreCliente = nombre ?: "N/A",
            hora = time,
            total = totalValue,
            estatus = estatus,
            cajaId = corteCaja,
            socioId = idSocio,
            tipoCliente = tipoVenta,
            fecha = date,
            clavePuntoVenta = clavePuntoVenta ?: "",
            numComensales = numComensales,
            productos = productos ?: emptyList(),
            pagos = pagos ?: emptyList()
        )
    }
}
