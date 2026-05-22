package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductosViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeProductos()
        sync()
    }

    private fun observeProductos() {
        viewModelScope.launch {
            productoRepository.getProductos().collect { lista ->
                _productos.value = lista
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            productoRepository.searchProductos(query).collect { lista ->
                _productos.value = lista
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productoRepository.sync()
            } catch (e: Exception) {
                Log.e("ProdVM", "Sync falló", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
