package com.example.sisvvapp.data.repository

import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.sisvv.mobile.network.dto.cajas.CajaDto
import com.sisvv.mobile.network.dto.productos.ModificadorSyncDto
import com.sisvv.mobile.network.dto.productos.ProductoDto
import com.sisvv.mobile.network.dto.socios.IntegranteDto
import com.sisvv.mobile.network.dto.socios.SocioDto

fun SocioDto.toSocioEntity() = SocioEntity(
    id = id,
    nombre = nombre,
    apellidoP = apellidoP,
    apellidoM = apellidoM,
    telefono = telefono,
    email = email,
    firmaAutorizada = firma,
    estatus = estatus,
    fotoUrl = fotoUrl
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
    id = id,
    clave = clave.toString(),
    descripcion = descripcion,
    precio = precio,
    categoria = categoria,
    imagenUrl = imagenUrl.ifEmpty { null },
    forzarCaptura = forzarCaptura,
    modifIncluidos = modifIncluidos,
    modifMaximos = modifMaximos
)

fun ProductoDto.toModificadorEntities(): List<ModificadorEntity> = modificadores.map { mod ->
    ModificadorEntity(
        id = mod.id,
        productoId = mod.productoId,
        nombre = mod.nombre,
        tipo = mod.tipo,
        precio = mod.precio,
        grupo = mod.grupo,
        incluido = mod.incluido
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
