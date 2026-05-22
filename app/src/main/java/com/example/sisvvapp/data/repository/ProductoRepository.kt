package com.example.sisvvapp.data.repository

import android.util.Log
import com.example.sisvvapp.data.local.dao.ProductoDao
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.network.ApiService
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val api: ApiService,
    private val productoDao: ProductoDao
) {

    fun getProductos(): Flow<List<ProductoEntity>> = productoDao.getAllProductos()

    fun searchProductos(query: String): Flow<List<ProductoEntity>> =
        productoDao.searchProductos(query)

    suspend fun sync(): Result<Unit> = runCatching {
        val response = api.getProductos()
        if (response.isSuccessful) {
            val productosDto = response.body().orEmpty()
            val productosEnt = productosDto.map { it.toProductoEntity() }
            val modificadoresEnt = productosDto.flatMap { it.toModificadorEntities() }

            productoDao.insertAllProductos(productosEnt)
            productoDao.insertAllModificadores(modificadoresEnt)

            Log.d("ProdRepo", "Sincronizados ${productosEnt.size} productos")
        } else {
            Log.w("ProdRepo", "Error sync productos: ${response.code()}")
        }
    }
}
