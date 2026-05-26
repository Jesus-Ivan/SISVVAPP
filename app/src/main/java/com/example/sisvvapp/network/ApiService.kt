package com.example.sisvvapp.network

import com.example.sisvvapp.network.dto.auth.LoginRequest
import com.example.sisvvapp.network.dto.auth.LoginResponse
import com.example.sisvvapp.network.dto.cajas.CajaDto
import com.example.sisvvapp.network.dto.productos.GrupoModificadorDto
import com.example.sisvvapp.network.dto.productos.ProductoDto
import com.example.sisvvapp.network.dto.socios.SocioDto
import com.example.sisvvapp.network.dto.ventas.VentaDto
import com.example.sisvvapp.network.dto.ventas.VentaRequest
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
    suspend fun getVentasAbiertas(): Response<List<VentaDto>>

    @POST("ventas")
    suspend fun crearVenta(
        @Body request: VentaRequest
    ): Response<VentaDto>

    @POST("ventas/{folio}/productos")
    suspend fun appendProductos(
        @Path("folio") folio: Int,
        @Body request: VentaRequest
    ): Response<VentaDto>

    @POST("logout")
    suspend fun logout(): Response<Unit>
}
