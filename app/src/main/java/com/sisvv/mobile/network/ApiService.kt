package com.sisvv.mobile.network

import com.sisvv.mobile.network.dto.auth.LoginRequest
import com.sisvv.mobile.network.dto.auth.LoginResponse
import com.sisvv.mobile.network.dto.cajas.CajaDto
import com.sisvv.mobile.network.dto.productos.GrupoModificadorDto
import com.sisvv.mobile.network.dto.productos.ProductoDto
import com.sisvv.mobile.network.dto.socios.SocioDto
import com.sisvv.mobile.network.dto.ventas.VentaDto
import com.sisvv.mobile.network.dto.ventas.VentaRequest
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
    suspend fun getCajaActiva(): Response<CajaDto>

    @GET("ventas")
    suspend fun getVentasAbiertas(): Response<List<VentaDto>>

    @POST("ventas")
    suspend fun crearVenta(
        @Body request: VentaRequest
    ): Response<VentaDto>
}
