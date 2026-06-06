package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.sisvvapp.data.local.view.VentaGlobalView
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaGlobalDao {
    @Query("SELECT * FROM v_ventas_global WHERE fechaFiltro = :fecha ORDER BY timestamp DESC")
    fun getVentasGlobales(fecha: String): Flow<List<VentaGlobalView>>

    @Query("SELECT * FROM v_ventas_global WHERE corteCaja = :corteCaja AND fechaFiltro = :fecha ORDER BY timestamp DESC")
    fun getVentasPorCorte(corteCaja: Int, fecha: String): Flow<List<VentaGlobalView>>
}
