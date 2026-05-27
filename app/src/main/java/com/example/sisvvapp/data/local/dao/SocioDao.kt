package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSocios(socios: List<SocioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIntegrantes(integrantes: List<IntegranteEntity>)

    @Transaction
    @Query("SELECT * FROM socios WHERE id = :id")
    suspend fun getSocioConIntegrantes(id: Int): SocioWithIntegrantes?

    @Query("SELECT * FROM socios WHERE id LIKE :term OR nombre LIKE :term OR apellido_p LIKE :term OR apellido_m LIKE :term")
    fun searchSocios(term: String): Flow<List<SocioEntity>>

    @Query("SELECT * FROM socios ORDER BY apellido_p, apellido_m, nombre")
    fun getAllSocios(): Flow<List<SocioEntity>>

    @Query("DELETE FROM socios")
    suspend fun deleteAll()
}

data class SocioWithIntegrantes(
    @Embedded val socio: SocioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "socio_id"
    )
    val integrantes: List<IntegranteEntity>
)
