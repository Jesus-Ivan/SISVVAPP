package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CajaActivaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCajaActiva(caja: CajaActivaEntity)

    @Query("SELECT * FROM cajas_activas WHERE activo = 1")
    fun getCajaActiva(): Flow<CajaActivaEntity?>

    @Query("SELECT * FROM cajas_activas WHERE activo = 1")
    fun getCajasAbiertas(): Flow<List<CajaActivaEntity>>

    // One-shot query: devuelve la lista actual sin Flow (para usar después de sync)
    @Query("SELECT * FROM cajas_activas WHERE activo = 1")
    suspend fun getCajasSnapshot(): List<CajaActivaEntity>

    @Query("DELETE FROM cajas_activas")
    suspend fun deleteAll()
}
