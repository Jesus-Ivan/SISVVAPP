package com.example.sisvvapp.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cajas_activas")
data class CajaActivaEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "fecha_apertura") val fechaApertura: String,
    @ColumnInfo(name = "fecha_cierre") val fechaCierre: String?,
    @ColumnInfo(name = "activo") val activo: Boolean,
    @ColumnInfo(name = "mesero_id") val meseroId: Int?,
    @ColumnInfo(name = "corte") val corte: Int? = null,
    @ColumnInfo(name = "cambio_inicial") val cambioInicial: Double? = null,
    @ColumnInfo(name = "clave_punto_venta") val clavePuntoVenta: String? = null
)
