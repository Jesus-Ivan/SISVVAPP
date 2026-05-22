package com.example.sisvvapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas_cola")
data class VentaColaEntity(
    @PrimaryKey val idTemporal: String,
    val tipoVenta: String,
    val idSocio: Int?,
    val nombreCliente: String,
    val nombreCaja: String,
    val productosJson: String,
    val fechaCreacion: Long,
    val totalVenta: Double,
    val estado: String
)
