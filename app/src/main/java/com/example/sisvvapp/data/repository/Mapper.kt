package com.example.sisvvapp.data.repository

import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.productos.ModificadorSyncDto
import com.example.sisvvapp.network.dto.productos.ProductoDto
import com.example.sisvvapp.network.dto.socios.IntegranteDto
import com.example.sisvvapp.network.dto.socios.SocioDto

fun SocioDto.toSocioEntity() = SocioEntity(
    id = id,
    nombre = nombre,
    apellidoP = apellidoP,
    apellidoM = apellidoM,
    telefono = null,
    email = null,
    firmaAutorizada = firma,
    estatus = membresia?.estado ?: "Inactivo",
    fotoUrl = imgPath,
    membresiaTipo = membresia?.clave ?: "Sin membresía"
)

fun SocioDto.toIntegranteEntities(): List<IntegranteEntity> = integrantes.map { int ->
    IntegranteEntity(
        id = int.id ?: 0,
        socioId = id,
        nombre = int.nombre,
        parentesco = int.parentesco,
        fotoUrl = int.fotoUrl
    )
}

fun ProductoDto.toProductoEntity() = ProductoEntity(
    id = 0,
    clave = clave.toString(),
    descripcion = descripcion,
    precio = costoUnitario,
    categoria = grupo ?: "Sin categoría",
    imagenUrl = null,
    forzarCaptura = gruposModificadores.any { it.forzarCaptura },
    modifIncluidos = gruposModificadores.firstOrNull()?.modifIncluidos ?: 0,
    modifMaximos = gruposModificadores.firstOrNull()?.modifMaximos ?: 0
)

fun ProductoDto.toModificadorEntities(): List<ModificadorEntity> = modificadoresOpciones.map { mod ->
    ModificadorEntity(
        id = mod.id,
        productoId = clave,
        nombre = mod.descripcion,
        tipo = "",
        precio = mod.precioOverride,
        grupo = mod.idGrupo.toString(),
        incluido = false
    )
}

fun CajaDto.toCajaActivaEntity() = CajaActivaEntity(
    id = id,
    nombre = nombre,
    fechaApertura = fechaApertura,
    fechaCierre = fechaCierre,
    activo = activo,
    meseroId = meseroId
)
