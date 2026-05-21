package com.sisvv.mobile.network.dto.productos

data class GrupoModificadorDto(
    val titulo: String,
    val requerido: Boolean,
    val tipo: String,
    val opciones: List<ModificadorDto>
)
