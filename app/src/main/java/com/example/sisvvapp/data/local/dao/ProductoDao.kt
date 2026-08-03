package com.example.sisvvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProductos(productos: List<ProductoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllModificadores(modificadores: List<ModificadorEntity>)

    @Transaction
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getProductoConModificadores(id: Int): ProductoConModificadores?

    @Query("SELECT * FROM productos WHERE descripcion LIKE :term ORDER BY descripcion")
    fun searchProductos(term: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos ORDER BY categoria, descripcion")
    fun getAllProductos(): Flow<List<ProductoEntity>>

    @Query("SELECT imagen_url FROM productos")
    suspend fun getAllProductosImagenes(): List<String?>

    @Query("DELETE FROM productos")
    suspend fun deleteAll()
}

data class ProductoConModificadores(
    @Embedded val producto: ProductoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "producto_id"
    )
    val modificadores: List<ModificadorEntity>
)
