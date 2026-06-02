package com.example.sisvvapp.network

import com.example.sisvvapp.network.dto.auth.LoginRequest
import com.example.sisvvapp.network.dto.auth.LoginResponse
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.productos.GrupoModificadorDto
import com.example.sisvvapp.network.dto.productos.ProductoDto
import com.example.sisvvapp.network.dto.socios.SocioDto
import com.example.sisvvapp.network.dto.ventas.TipoPagoDto
import com.example.sisvvapp.network.dto.ventas.VentaRequest
import com.example.sisvvapp.network.dto.ventas.VentaResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("sync/socios")
    suspend fun getSocios(): Response<List<SocioDto>>

    @GET("sync/productos")
    suspend fun getProductos(): Response<List<ProductoDto>>

    @GET("productos/{clave}/modificadores")
    suspend fun getModificadores(
        @Path("clave") clave: Int
    ): Response<List<GrupoModificadorDto>>

    @GET("cajas/activas")
    suspend fun getCajasActivas(): Response<List<CajaDto>>

    @GET("ventas")
    suspend fun getVentas(
        @Query("fecha") fecha: String?,
        @Query("corte_caja") corteCaja: Int?
    ): Response<List<VentaResponse>>

    @POST("ventas")
    suspend fun crearVenta(
        @Body request: VentaRequest
    ): Response<VentaResponse>

    @POST("ventas/{folio}/productos")
    suspend fun appendProductos(
        @Path("folio") folio: Int,
        @Body request: VentaRequest
    ): Response<VentaResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("tipos-pago")
    suspend fun getTiposPago(): Response<List<TipoPagoDto>>
}
