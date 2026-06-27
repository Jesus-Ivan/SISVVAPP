package com.example.sisvvapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sisvvapp.data.local.AppDatabase
import com.example.sisvvapp.data.local.SessionManager
import com.example.sisvvapp.data.repository.CajaRepository
import com.example.sisvvapp.data.repository.ProductoRepository
import com.example.sisvvapp.data.repository.SocioRepository
import com.example.sisvvapp.data.repository.VentaRepository
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.network.RetrofitClient

class SisvvViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val api get() = RetrofitClient.create(context)
    private val db = AppDatabase.getInstance(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SisvvViewModel::class.java) ->
                SisvvViewModel(context.applicationContext) as T

            modelClass.isAssignableFrom(SociosViewModel::class.java) ->
                SociosViewModel(SocioRepository(api, db, db.socioDao(), context)) as T

            modelClass.isAssignableFrom(CajaViewModel::class.java) ->
                CajaViewModel(
                    CajaRepository(api, db, db.cajaActivaDao()),
                    SocioRepository(api, db, db.socioDao(), context),
                    ProductoRepository(api, db, db.productoDao(), db.grupoModificadorDao()),
                    SessionManager.getInstance(context)
                ) as T

            modelClass.isAssignableFrom(VentasViewModel::class.java) ->
                VentasViewModel(VentaRepository(api, db, context)) as T

            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel(SessionManager.getInstance(context)) as T

            modelClass.isAssignableFrom(NuevaVentaViewModel::class.java) ->
                NuevaVentaViewModel(
                    SocioRepository(api, db, db.socioDao(), context),
                    com.example.sisvvapp.data.repository.TipoVentaRepository(api, db, db.tipoVentaDao())
                ) as T

            modelClass.isAssignableFrom(CarritoViewModel::class.java) ->
                CarritoViewModel(
                    ProductoRepository(api, db, db.productoDao(), db.grupoModificadorDao()),
                    VentaRepository(api, db, context)
                ) as T


            modelClass.isAssignableFrom(ModificadoresViewModel::class.java) ->
                ModificadoresViewModel(ProductoRepository(api, db, db.productoDao(), db.grupoModificadorDao())) as T

            modelClass.isAssignableFrom(VentasPendientesViewModel::class.java) ->
                VentasPendientesViewModel(VentaRepository(api, db, context), context) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}