package com.example.sisvvapp.network.dto.ventas

import com.google.gson.annotations.SerializedName

data class TransferirProductoRequest(
    @SerializedName("folio_destino") val folioDestino: Int,
    val chunk: Long
)
