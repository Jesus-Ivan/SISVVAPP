package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sisvvapp.data.local.entity.GrupoModificadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoModificadorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grupos: List<GrupoModificadorEntity>)

    @Query("SELECT * FROM grupos_modificador_producto WHERE clave_producto = :claveProducto")
    fun getGruposPorProducto(claveProducto: Int): Flow<List<GrupoModificadorEntity>>

    @Query("SELECT * FROM grupos_modificador_producto WHERE clave_producto = :claveProducto")
    suspend fun getGruposPorProductoSync(claveProducto: Int): List<GrupoModificadorEntity>

    @Query("DELETE FROM grupos_modificador_producto")
    suspend fun deleteAll()
}
