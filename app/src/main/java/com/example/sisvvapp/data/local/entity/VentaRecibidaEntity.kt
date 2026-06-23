package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ventas_recibidas",
    indices = [
        Index(value = ["corte_caja"]),
        Index(value = ["estado"])
    ]
)
data class VentaRecibidaEntity(
    @PrimaryKey val folio: Int,
    val fecha: String,
    val total: Double,
    val estado: String,
    @ColumnInfo(name = "cliente_nombre") val clienteNombre: String?,
    @ColumnInfo(name = "socio_id") val socioId: Int?,
    @ColumnInfo(name = "clave_punto_venta") val clavePuntoVenta: String,
    @ColumnInfo(name = "corte_caja") val corteCaja: Int,
    @ColumnInfo(name = "productos_json") val productosJson: String,
    @ColumnInfo(name = "pagos_json") val pagosJson: String = "[]",
    @ColumnInfo(name = "num_comensales") val numComensales: Int? = null,
    @ColumnInfo(name = "fecha_guardado") val fechaGuardado: Long = System.currentTimeMillis()
)
