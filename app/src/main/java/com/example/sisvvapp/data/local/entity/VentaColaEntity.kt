package com.example.sisvvapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ventas_cola",
    indices = [
        Index(value = ["estado"]),
        Index(value = ["folioExistente"])
    ]
)
data class VentaColaEntity(
    @PrimaryKey val idTemporal: String,
    val tipoVenta: String,
    val idSocio: Int?,
    val nombreCliente: String,
    val corteCaja: Int,
    val clavePuntoVenta: String,
    val nombreCaja: String,
    val productosJson: String,
    val fechaCreacion: Long,
    val totalVenta: Double,
    val estado: String,
    val folioExistente: Int? = null,
    val pagosJson: String? = null,
    val intentos: Int = 0
)
