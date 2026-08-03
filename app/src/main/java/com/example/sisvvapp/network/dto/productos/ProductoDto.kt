package com.example.sisvvapp.network.dto.productos

import com.google.gson.annotations.SerializedName

data class ProductoDto(
    val clave: Int,
    val descripcion: String,
    @SerializedName("costo_unitario")     val costoUnitario: Double = 0.0,
    @SerializedName("precio")             val precio: Double = 0.0,
    @SerializedName("print_default")      val printDefault: Boolean = false,
    @SerializedName("img_path")           val imgPath: String? = null,
    @SerializedName("id_grupo")           val idGrupo: Int?,
    val grupo: String?,
    @SerializedName("id_subgrupo")        val idSubgrupo: Int?,
    val subgrupo: String?,
    @SerializedName("grupos_modificadores") val gruposModificadores: List<GrupoModificadorDto> = emptyList(),
    @SerializedName("modificadores_opciones") val modificadoresOpciones: List<ModificadorSyncDto> = emptyList()
)
