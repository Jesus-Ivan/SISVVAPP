package com.sisvv.mobile.network.dto.productos

data class ItemCarritoDto(
    val producto: ProductoDto,
    @SerializedName("modificadores_seleccionados") val modificadoresSeleccionados: List<ModificadorDto> = emptyList(),
    val cantidad: Int = 1
)
