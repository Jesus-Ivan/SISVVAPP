package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.data.local.entity.GrupoModificadorEntity
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.productos.ModificadorSyncDto
import com.example.sisvvapp.network.dto.productos.ProductoDto
import com.example.sisvvapp.network.dto.socios.IntegranteDto
import com.example.sisvvapp.network.dto.socios.SocioDto

fun String?.toMembresiaDescripcion(): String? = when (this) {
    "CG-F" -> "CG-FAMILIAR"
    "CG-I" -> "CG-INDIVIDUAL"
    "CG-P" -> "CG-PAREJA"
    "CG-V-S" -> "CG-VIUDA S.HIJOS"
    "CG-V-C" -> "CG-VIUDA C.HIJOS"
    "CC-F" -> "CC-FAMILIAR"
    "CC-I" -> "CC-INDIVIDUAL"
    "CC-P" -> "CC-PAREJA"
    "CC-V-S" -> "CC-VIUDA S.HIJOS"
    "CC-V-C" -> "CC-VIUDA C.HIJOS"
    "INT" -> "INTERMITENTE"
    "EST" -> "ESTABLE"
    "COR" -> "CORRIDA"
    else -> this
}

fun SocioDto.toSocioEntity() = SocioEntity(
    id = id,
    nombre = nombre,
    apellidoP = apellidoP ?: "",
    apellidoM = apellidoM ?: "",
    telefono = null,
    email = null,
    firmaAutorizada = firma,
    estatus = membresia?.estado ?: "Inactivo",
    fotoUrl = imgPath,
    numAccion = numAccion,
    membresiaTipo = membresia?.clave?.toMembresiaDescripcion() ?: "Sin membresía"
)

fun SocioDto.toIntegranteEntities(): List<IntegranteEntity> = integrantes.mapIndexed { index, int ->
    Log.d("MAPPER", "Integrante: id=${int.id}, nombre='${int.nombre}', apellidoP='${int.apellidoP}', apellidoM='${int.apellidoM}', parentesco='${int.parentesco}', fotoUrl=${int.fotoUrl}")
    IntegranteEntity(
        id = int.id ?: -(index + 1),
        socioId = id,
        nombre = int.nombre ?: "",
        apellidoP = int.apellidoP,
        apellidoM = int.apellidoM,
        parentesco = int.parentesco ?: "",
        fotoUrl = int.fotoUrl
    )
}

fun ProductoDto.toProductoEntity() = ProductoEntity(
    id = clave,
    clave = clave.toString(),
    descripcion = descripcion,
    precio = if (precio > 0) precio else costoUnitario,
    categoria = grupo ?: "Sin categoría",
    imagenUrl = null,
    forzarCaptura = gruposModificadores.any { it.forzarCaptura },
    modifIncluidos = gruposModificadores.maxOfOrNull { it.modifIncluidos } ?: 0,
    modifMaximos = gruposModificadores.maxOfOrNull { it.modifMaximos } ?: 0
)

fun ProductoDto.toModificadorEntities(): List<ModificadorEntity> = modificadoresOpciones.map { mod ->
    ModificadorEntity(
        id = mod.id,
        productoId = clave,
        claveModificador = mod.claveModificador,
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
    meseroId = meseroId,
    corte = corte,
    cambioInicial = cambioInicial,
    clavePuntoVenta = clavePuntoVenta
)

fun ProductoDto.toGrupoModificadorEntities(): List<GrupoModificadorEntity> = gruposModificadores.map { gm ->
    GrupoModificadorEntity(
        idGrupo = gm.idGrupo,
        claveProducto = clave,
        descripcionGrupo = gm.descripcion,
        modifIncluidos = gm.modifIncluidos,
        modifMaximos = gm.modifMaximos,
        forzarCaptura = gm.forzarCaptura
    )
}
