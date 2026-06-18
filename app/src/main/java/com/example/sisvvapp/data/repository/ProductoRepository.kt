package com.example.sisvvapp.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.dao.GrupoModificadorDao
import com.example.sisvvapp.data.local.dao.ProductoConModificadores
import com.example.sisvvapp.data.local.dao.ProductoDao
import com.example.sisvvapp.data.local.entity.GrupoModificadorEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val api: ApiService,
    private val db: AppDatabase,
    private val productoDao: ProductoDao,
    private val grupoModificadorDao: GrupoModificadorDao
) {

    fun getProductos(): Flow<List<ProductoEntity>> = productoDao.getAllProductos()

    fun searchProductos(query: String): Flow<List<ProductoEntity>> =
        productoDao.searchProductos(query)

    suspend fun getProductoConModificadores(clave: Int): ProductoConModificadores? =
        productoDao.getProductoConModificadores(clave)

    suspend fun getGruposPorProducto(claveProducto: Int): List<GrupoModificadorEntity> =
        grupoModificadorDao.getGruposPorProductoSync(claveProducto)

    suspend fun sync(): Result<Unit> {
        return try {
            val response = api.getProductos()
            if (response.isSuccessful) {
                val productosDto = response.body().orEmpty()
                val productosEnt = productosDto.map { it.toProductoEntity() }
                val modificadoresEnt = productosDto.flatMap { it.toModificadorEntities() }
                val gruposEnt = productosDto.flatMap { it.toGrupoModificadorEntities() }

                db.withTransaction {
                    productoDao.deleteAll()
                    grupoModificadorDao.deleteAll()

                    productoDao.insertAllProductos(productosEnt)
                    productoDao.insertAllModificadores(modificadoresEnt)
                    grupoModificadorDao.insertAll(gruposEnt)
                }

                Log.d("ProdRepo", "Sincronizados ${productosEnt.size} productos, ${gruposEnt.size} grupos")
            } else {
                Log.w("ProdRepo", "Error sync productos: ${response.code()}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProdRepo", "Excepción en sync", e)
            Result.failure(e)
        }
    }
}
