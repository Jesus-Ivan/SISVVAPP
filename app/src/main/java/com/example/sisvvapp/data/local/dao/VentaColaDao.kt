package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaColaDao {
    @Query("SELECT * FROM ventas_cola ORDER BY fechaCreacion ASC")
    fun getAllFlow(): Flow<List<VentaColaEntity>>

    @Query("SELECT * FROM ventas_cola")
    suspend fun getAll(): List<VentaColaEntity>

    @Query("SELECT * FROM ventas_cola WHERE estado = 'PENDIENTE' OR estado = 'ERROR' ORDER BY fechaCreacion ASC")
    suspend fun getParaSincronizar(): List<VentaColaEntity>

    @Query("SELECT * FROM ventas_cola WHERE estado = 'PENDIENTE' ORDER BY fechaCreacion ASC")
    suspend fun getPendientes(): List<VentaColaEntity>

    @Query("SELECT * FROM ventas_cola WHERE idTemporal = :id")
    suspend fun getById(id: String): VentaColaEntity?

    @Query("SELECT * FROM ventas_cola WHERE folioExistente = :folio AND (estado = 'PENDIENTE' OR estado = 'ERROR') LIMIT 1")
    suspend fun getByFolioExistente(folio: Int): VentaColaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(venta: VentaColaEntity)

    @Query("UPDATE ventas_cola SET estado = :estado WHERE idTemporal = :id")
    suspend fun updateEstado(id: String, estado: String)

    @Query("DELETE FROM ventas_cola WHERE idTemporal = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM ventas_cola WHERE estado = 'PENDIENTE'")
    fun countPendientesFlow(): Flow<Int>
}
