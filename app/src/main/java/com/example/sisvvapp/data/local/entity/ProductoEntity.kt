package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "clave") val clave: String,
    @ColumnInfo(name = "descripcion") val descripcion: String,
    @ColumnInfo(name = "precio") val precio: Double,
    @ColumnInfo(name = "categoria") val categoria: String,
    @ColumnInfo(name = "imagen_url") val imagenUrl: String?,
    @ColumnInfo(name = "forzar_captura") val forzarCaptura: Boolean,
    @ColumnInfo(name = "modif_incluidos") val modifIncluidos: Int,
    @ColumnInfo(name = "modif_maximos") val modifMaximos: Int
)
