package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.LoginRequest
import com.oportunyfam_mobile.model.LoginResponse
import com.oportunyfam_mobile.model.UsuarioRequest
import com.oportunyfam_mobile.model.UsuarioResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UsuarioService {
    @Headers("Content-Type: application/json")
    @POST("usuarios")
    suspend fun criar(@Body request: UsuarioRequest): Response<UsuarioResponse>

    // PUT /v1/usuario/:id - Envia o Request, retorna o Response
    @PUT("usuarios/{id}")
    suspend fun atualizar(@Path("id") id: Int, @Body usuario: UsuarioRequest): Response<UsuarioResponse>

    // GET /v1/usuario - Retorna Lista de Responses
    @GET("usuarios")
    suspend fun listarTodos(): Response<List<UsuarioResponse>>

    // GET /v1/usuario/:id - Retorna um Response
    @GET("usuarios/{id}")
    suspend fun buscarPorId(@Path("id") id: Int): Response<UsuarioResponse>

    // POST /v1/usuarios/login - Usa LoginRequest, retorna LoginResponse
    @POST("usuarios/login")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<LoginResponse>

    // DELETE /v1/usuario/:id - Sem mudança
    @DELETE("usuarios/{id}")
    suspend fun deletar(@Path("id") id: Int): Response<Unit>
}