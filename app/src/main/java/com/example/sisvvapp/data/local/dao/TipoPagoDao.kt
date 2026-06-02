package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sisvvapp.data.local.entity.TipoPagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoPagoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tipos: List<TipoPagoEntity>)

    @Query("SELECT * FROM tipos_pago WHERE activo = 1 ORDER BY nombre")
    fun getTiposPago(): Flow<List<TipoPagoEntity>>

    @Query("SELECT * FROM tipos_pago WHERE activo = 1 ORDER BY nombre")
    suspend fun getTiposPagoSync(): List<TipoPagoEntity>

    @Query("DELETE FROM tipos_pago")
    suspend fun deleteAll()
}
