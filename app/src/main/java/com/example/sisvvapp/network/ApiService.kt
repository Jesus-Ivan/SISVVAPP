package com.example.sisvvapp.network

import com.example.sisvvapp.network.dto.auth.LoginRequest
import com.example.sisvvapp.network.dto.auth.LoginResponse
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.productos.ProductoDto
import com.example.sisvvapp.network.dto.socios.SocioDto
import com.example.sisvvapp.network.dto.ventas.TipoPagoDto
import com.example.sisvvapp.network.dto.ventas.TransferirProductoRequest
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

    @POST("ventas/{folio}/transferir-producto")
    suspend fun transferirProducto(
        @Path("folio") folio: Int,
        @Body request: TransferirProductoRequest
    ): Response<Unit>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("ventas/{folio}")
    suspend fun getVentaDetalle(
        @Path("folio") folio: Int
    ): Response<VentaResponse>

    @GET("tipos-pago")
    suspend fun getTiposPago(): Response<List<TipoPagoDto>>

    @GET("sync/tipos-venta")
    suspend fun getTiposVenta(): Response<List<String>>
}
