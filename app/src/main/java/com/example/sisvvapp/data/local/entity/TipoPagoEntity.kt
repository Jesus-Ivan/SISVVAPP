package com.example.sisvvapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_pago")
data class TipoPagoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val requiereSocio: Boolean = false,
    val requiereFirma: Boolean = false,
    val activo: Boolean = true
)
