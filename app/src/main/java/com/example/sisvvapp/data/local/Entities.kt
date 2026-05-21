package com.example.sisvvapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── Entidades de Catálogo ────────────────────────────────────────────────────

@Entity(tableName = "socios")
data class SocioEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val apellidoP: String,
    val apellidoM: String,
    val numAccion: Int?,
    val firma: Boolean,                   // true = FIRMA AUTORIZADA
    val imgPath: String?,
    val claveMembresía: String?,
    val estadoMembresía: String?,         // MEN / INA / ANU / CAN
    val descripcionMembresía: String?     // "Familiar", "Individual", etc.
)

@Entity(
    tableName = "integrantes",
    foreignKeys = [ForeignKey(
        entity = SocioEntity::class,
        parentColumns = ["id"],
        childColumns = ["idSocio"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("idSocio")]
)
data class IntegranteEntity(
    @PrimaryKey val id: Int,
    val idSocio: Int,
    val nombre: String,
    val apellidoP: String?,
    val apellidoM: String?,
    val parentesco: String?,
    val imgPath: String?
)

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val clave: Int,
    val descripcion: String,
    val precioConImpuestos: Double,
    val idGrupo: Int?,
    val grupo: String?,
    val idSubgrupo: Int?,
    val subgrupo: String?,
    val printDefault: Boolean,
    val tieneModificadores: Boolean,
    val imagenUrl: String?
)

@Entity(tableName = "grupos_modificadores")
data class GrupoModificadorEntity(
    @PrimaryKey val idGrupo: Int,
    val claveProducto: Int,
    val descripcion: String,
    val modifIncluidos: Int,    // cantidad incluida gratis
    val modifMaximos: Int,      // máximo seleccionable
    val forzarCaptura: Boolean  // si true, obliga al menos 1 selección
)

@Entity(tableName = "modificadores")
data class ModificadorEntity(
    @PrimaryKey val id: Int,
    val idGrupo: Int,
    val claveProducto: Int,
    val claveModificador: Int,
    val descripcion: String,
    val precioOverride: Double,
    val printDefault: Boolean
)

// ─── Cola de Ventas Offline ───────────────────────────────────────────────────

@Entity(tableName = "ventas_cola")
data class VentaColaEntity(
    @PrimaryKey val idTemporal: String,   // UUID v4 generado localmente
    val tipoVenta: String,                // socio / invitado / general
    val idSocio: Int?,
    val nombreCliente: String,
    val nombreCaja: String,
    val productosJson: String,            // JSON serializado de la lista de productos
    val fechaCreacion: Long,              // System.currentTimeMillis()
    val totalVenta: Double,
    val estado: String                    // PENDIENTE / SINCRONIZADA / ERROR
)
