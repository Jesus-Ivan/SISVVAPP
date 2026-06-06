package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sisvvapp.data.local.entity.VentaRecibidaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaRecibidaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ventas: List<VentaRecibidaEntity>)

    // Filtra por corte Y por prefijo de fecha ("yyyy-MM-dd") y solo ventas abiertas
    @Query("SELECT * FROM ventas_recibidas WHERE corte_caja = :corteCaja AND fecha LIKE :fechaPrefix || '%' AND estado = 'Abierta' ORDER BY folio DESC")
    fun getVentasPorCorteYFecha(corteCaja: Int, fechaPrefix: String): Flow<List<VentaRecibidaEntity>>

    // Filtra solo por fecha y solo ventas abiertas (cuando no hay corte seleccionado)
    @Query("SELECT * FROM ventas_recibidas WHERE fecha LIKE :fechaPrefix || '%' AND estado = 'Abierta' ORDER BY folio DESC")
    fun getVentasPorFecha(fechaPrefix: String): Flow<List<VentaRecibidaEntity>>

    @Query("SELECT * FROM ventas_recibidas WHERE corte_caja = :corteCaja ORDER BY folio DESC")
    fun getVentasPorCorte(corteCaja: Int): Flow<List<VentaRecibidaEntity>>

    @Query("SELECT * FROM ventas_recibidas ORDER BY folio DESC")
    fun getAllVentas(): Flow<List<VentaRecibidaEntity>>

    @Query("SELECT * FROM ventas_recibidas WHERE folio = :folio")
    suspend fun getVentaPorFolio(folio: Int): VentaRecibidaEntity?

    @Query("SELECT * FROM ventas_recibidas WHERE folio = :folio")
    fun getVentaPorFolioFlow(folio: Int): Flow<VentaRecibidaEntity?>

    @Query("DELETE FROM ventas_recibidas WHERE fecha LIKE :fechaPrefix || '%'")
    suspend fun deleteByFecha(fechaPrefix: String)

    @Query("DELETE FROM ventas_recibidas")
    suspend fun deleteAll()
}