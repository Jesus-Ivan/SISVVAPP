package com.example.sisvvapp.network.dto.productos

import com.google.gson.annotations.SerializedName

data class GrupoModificadorDto(
    @SerializedName("id_grupo")       val idGrupo: Int,
    val descripcion: String,
    @SerializedName("modif_incluidos") val modifIncluidos: Int,
    @SerializedName("modif_maximos")   val modifMaximos: Int,
    @SerializedName("forzar_captura")  val forzarCaptura: Boolean
)
