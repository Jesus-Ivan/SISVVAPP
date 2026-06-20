package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "socios")
data class SocioEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "apellido_p") val apellidoP: String,
    @ColumnInfo(name = "apellido_m") val apellidoM: String,
    @ColumnInfo(name = "telefono") val telefono: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "firma_autorizada") val firmaAutorizada: Boolean,
    @ColumnInfo(name = "estatus") val estatus: String,
    @ColumnInfo(name = "foto_url") val fotoUrl: String?,
    @ColumnInfo(name = "num_accion") val numAccion: Int?,
    @ColumnInfo(name = "membresia_tipo") val membresiaTipo: String?,
    @ColumnInfo(name = "membresias_json") val membresiasJson: String? = null
)
