package com.example.sisvvapp.network.dto.productos

import com.google.gson.annotations.SerializedName

data class ModificadorSyncDto(
    val id: Int,
    @SerializedName("id_grupo")          val idGrupo: Int,
    @SerializedName("clave_modificador") val claveModificador: Int,
    val descripcion: String,
    @SerializedName("precio_override")   val precioOverride: Double,
    @SerializedName("print_default")     val printDefault: Boolean
)
