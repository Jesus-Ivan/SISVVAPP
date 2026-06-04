package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sisvvapp.data.local.entity.TipoVentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoVentaDao {
    @Query("SELECT * FROM tipos_venta")
    fun getAllTiposVentaFlow(): Flow<List<TipoVentaEntity>>

    @Query("SELECT * FROM tipos_venta")
    suspend fun getAllTiposVentaSync(): List<TipoVentaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiposVenta: List<TipoVentaEntity>)

    @Query("DELETE FROM tipos_venta")
    suspend fun deleteAll()
}
