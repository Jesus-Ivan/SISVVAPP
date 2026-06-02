package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "grupos_modificador_producto",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["clave_producto"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GrupoModificadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_grupo") val idGrupo: Int,
    @ColumnInfo(name = "clave_producto") val claveProducto: Int,
    @ColumnInfo(name = "descripcion_grupo") val descripcionGrupo: String,
    @ColumnInfo(name = "modif_incluidos") val modifIncluidos: Int,
    @ColumnInfo(name = "modif_maximos") val modifMaximos: Int,
    @ColumnInfo(name = "forzar_captura") val forzarCaptura: Boolean
)
