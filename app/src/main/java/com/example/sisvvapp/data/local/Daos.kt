package com.example.sisvvapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Socio DAO ────────────────────────────────────────────────────────────────
@Dao
interface SocioDao {
    @Query("SELECT * FROM socios ORDER BY apellidoP, apellidoM, nombre")
    fun getAllFlow(): Flow<List<SocioEntity>>

    @Query("SELECT * FROM socios WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SocioEntity?

    @Query("""
        SELECT * FROM socios
        WHERE id = :query
           OR nombre LIKE '%' || :query || '%'
           OR apellidoP LIKE '%' || :query || '%'
           OR apellidoM LIKE '%' || :query || '%'
        ORDER BY apellidoP, nombre
    """)
    fun searchFlow(query: String): Flow<List<SocioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(socios: List<SocioEntity>)

    @Query("DELETE FROM socios")
    suspend fun deleteAll()
}

// ─── Integrante DAO ───────────────────────────────────────────────────────────
@Dao
interface IntegranteDao {
    @Query("SELECT * FROM integrantes WHERE idSocio = :socioId")
    suspend fun getByParent(socioId: Int): List<IntegranteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(integrantes: List<IntegranteEntity>)

    @Query("DELETE FROM integrantes")
    suspend fun deleteAll()
}

// ─── Producto DAO ─────────────────────────────────────────────────────────────
@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY grupo, descripcion")
    fun getAllFlow(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE descripcion LIKE '%' || :q || '%' ORDER BY descripcion")
    fun searchFlow(q: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE clave = :clave LIMIT 1")
    suspend fun getByClave(clave: Int): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoEntity>)

    @Query("DELETE FROM productos")
    suspend fun deleteAll()
}

// ─── Grupo Modificador DAO ────────────────────────────────────────────────────
@Dao
interface GrupoModificadorDao {
    @Query("SELECT * FROM grupos_modificadores WHERE claveProducto = :clave ORDER BY idGrupo")
    suspend fun getByProducto(clave: Int): List<GrupoModificadorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grupos: List<GrupoModificadorEntity>)

    @Query("DELETE FROM grupos_modificadores")
    suspend fun deleteAll()
}

// ─── Modificador DAO ──────────────────────────────────────────────────────────
@Dao
interface ModificadorDao {
    @Query("SELECT * FROM modificadores WHERE claveProducto = :clave AND idGrupo = :grupo ORDER BY id")
    suspend fun getByProductoAndGrupo(clave: Int, grupo: Int): List<ModificadorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(modificadores: List<ModificadorEntity>)

    @Query("DELETE FROM modificadores")
    suspend fun deleteAll()
}

// ─── Venta Cola DAO ───────────────────────────────────────────────────────────
@Dao
interface VentaColaDao {
    @Query("SELECT * FROM ventas_cola ORDER BY fechaCreacion ASC")
    fun getAllFlow(): Flow<List<VentaColaEntity>>

    @Query("SELECT * FROM ventas_cola WHERE estado = 'PENDIENTE' ORDER BY fechaCreacion ASC")
    suspend fun getPendientes(): List<VentaColaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(venta: VentaColaEntity)

    @Query("UPDATE ventas_cola SET estado = :estado WHERE idTemporal = :id")
    suspend fun updateEstado(id: String, estado: String)

    @Query("DELETE FROM ventas_cola WHERE idTemporal = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM ventas_cola WHERE estado = 'PENDIENTE'")
    fun countPendientesFlow(): Flow<Int>
}
