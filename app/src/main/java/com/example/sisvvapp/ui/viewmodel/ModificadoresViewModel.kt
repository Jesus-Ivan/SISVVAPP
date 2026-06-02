package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.entity.GrupoModificadorEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModificadoresViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _grupos = MutableStateFlow<List<GrupoModificadorEntity>>(emptyList())
    val grupos: StateFlow<List<GrupoModificadorEntity>> = _grupos

    private val _modificadores = MutableStateFlow<List<ModificadorEntity>>(emptyList())
    val modificadores: StateFlow<List<ModificadorEntity>> = _modificadores

    fun cargarModificadores(productoId: Int) {
        viewModelScope.launch {
            _grupos.value = productoRepository.getGruposPorProducto(productoId)
            val result = productoRepository.getProductoConModificadores(productoId)
            _modificadores.value = result?.modificadores ?: emptyList()
            Log.d("ModificadoresVM", "Cargados ${_grupos.value.size} grupos, ${_modificadores.value.size} modificadores para producto $productoId")
        }
    }
}
