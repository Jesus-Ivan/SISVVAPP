package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "modificadores",
    indices = [Index("producto_id")],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["producto_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ModificadorEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "producto_id") val productoId: Int,
    @ColumnInfo(name = "clave_modificador") val claveModificador: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "tipo") val tipo: String,
    @ColumnInfo(name = "precio") val precio: Double,
    @ColumnInfo(name = "grupo") val grupo: String,
    @ColumnInfo(name = "incluido") val incluido: Boolean
)
