package com.example.sisvvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "integrantes",
    indices = [Index("socio_id")],
    foreignKeys = [
        ForeignKey(
            entity = SocioEntity::class,
            parentColumns = ["id"],
            childColumns = ["socio_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IntegranteEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "socio_id") val socioId: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "apellido_p") val apellidoP: String?,
    @ColumnInfo(name = "apellido_m") val apellidoM: String?,
    @ColumnInfo(name = "parentesco") val parentesco: String,
    @ColumnInfo(name = "foto_url") val fotoUrl: String?
)
