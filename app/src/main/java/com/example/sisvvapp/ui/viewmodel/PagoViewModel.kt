package com.example.sisvvapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvvapp.data.local.dao.TipoPagoDao
import com.example.sisvvapp.data.local.entity.TipoPagoEntity
import com.example.sisvvapp.network.ApiService
import com.example.sisvvapp.network.dto.ventas.PagoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PagoItem(
    val tipoPago: TipoPagoEntity,
    val monto: Double,
    val propina: Double = 0.0
)

class PagoViewModel(
    private val tipoPagoDao: TipoPagoDao,
    private val api: ApiService
) : ViewModel() {

    private val _tiposPago = MutableStateFlow<List<TipoPagoEntity>>(emptyList())
    val tiposPago: StateFlow<List<TipoPagoEntity>> = _tiposPago

    private val _pagos = MutableStateFlow<List<PagoItem>>(emptyList())
    val pagos: StateFlow<List<PagoItem>> = _pagos

    private val _montoTotal = MutableStateFlow(0.0)
    val montoTotal: StateFlow<Double> = _montoTotal

    init {
        observeTiposPago()
        syncTiposPago()
    }

    private fun observeTiposPago() {
        viewModelScope.launch {
            tipoPagoDao.getTiposPago().collect { lista ->
                _tiposPago.value = lista
            }
        }
    }

    fun syncTiposPago() {
        viewModelScope.launch {
            try {
                val response = api.getTiposPago()
                if (response.isSuccessful) {
                    val tipos = response.body()?.map { dto ->
                        TipoPagoEntity(
                            id = dto.id,
                            nombre = dto.nombre,
                            requiereSocio = dto.requiereSocio,
                            requiereFirma = dto.requiereFirma,
                            activo = dto.activo
                        )
                    } ?: emptyList()
                    tipoPagoDao.deleteAll()
                    tipoPagoDao.insertAll(tipos)
                }
            } catch (e: Exception) {
                Log.w("PagoVM", "Error al sincronizar tipos de pago", e)
            }
        }
    }

    fun agregarPago(tipoPago: TipoPagoEntity, monto: Double, propina: Double = 0.0) {
        val item = PagoItem(tipoPago = tipoPago, monto = monto, propina = propina)
        _pagos.value = _pagos.value + item
        _montoTotal.value = _pagos.value.sumOf { it.monto + it.propina }
    }

    fun eliminarPago(index: Int) {
        _pagos.value = _pagos.value.toMutableList().apply { removeAt(index) }
        _montoTotal.value = _pagos.value.sumOf { it.monto + it.propina }
    }

    fun toPagoRequests(): List<PagoRequest> {
        return _pagos.value.map { item ->
            PagoRequest(
                idTipoPago = item.tipoPago.id,
                nombre = item.tipoPago.nombre,
                monto = item.monto,
                propina = item.propina
            )
        }
    }

    fun isPagosValidos(totalVenta: Double): Boolean {
        if (_pagos.value.isEmpty()) return false
        val totalPagos = _pagos.value.sumOf { it.monto + it.propina }
        return totalPagos == totalVenta
    }

    fun limpiarPagos() {
        _pagos.value = emptyList()
        _montoTotal.value = 0.0
    }
}
