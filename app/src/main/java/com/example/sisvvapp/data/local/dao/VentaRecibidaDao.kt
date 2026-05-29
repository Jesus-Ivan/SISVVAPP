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

    @Query("SELECT * FROM ventas_recibidas WHERE corte_caja = :corteCaja ORDER BY folio DESC")
    fun getVentasPorCorte(corteCaja : Int):
    Flow<List<VentaRecibidaEntity>>

    @Query("SELECT * FROM ventas_recibidas ORDER BY folio DESC")
    fun getAllVentas(): Flow<List<VentaRecibidaEntity>>

    @Query("SELECT * FROM ventas_recibidas WHERE folio = :folio")
    suspend fun getVentaPorFolio(folio: Int): VentaRecibidaEntity?

    @Query("DELETE FROM ventas_recibidas")
    suspend fun deleteAll()
}