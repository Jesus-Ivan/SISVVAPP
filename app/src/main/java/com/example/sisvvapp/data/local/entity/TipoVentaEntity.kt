package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_venta")
data class TipoVentaEntity(
    @PrimaryKey
    @ColumnInfo(name = "nombre")
    val nombre: String
)
