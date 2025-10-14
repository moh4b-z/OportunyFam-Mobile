package com.example.oportunyfam.Service

import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam.model.LoginResponse
import com.example.oportunyfam.model.UsuarioRequest
import com.example.oportunyfam.model.UsuarioResponse
import retrofit2.Call
import retrofit2.http.*

interface UsuarioService {

    // POST /v1/usuario - Envia o Request, retorna o Response
    @POST("usuario")
    suspend fun criar(@Body request: UsuarioRequest): Call<UsuarioResponse>

    // PUT /v1/usuario/:id - Envia o Request, retorna o Response
    @PUT("usuario/{id}")
    fun atualizar(@Path("id") id: Int, @Body usuario: UsuarioRequest): Call<UsuarioResponse>

    // GET /v1/usuario - Retorna Lista de Responses
    @GET("usuario")
    fun listarTodos(): Call<List<UsuarioResponse>>

    // GET /v1/usuario/:id - Retorna um Response
    @GET("usuario/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<UsuarioResponse>

    // POST /v1/usuario/login - Usa LoginRequest, retorna LoginResponse
    @POST("usuario/login")
    fun loginUsuario(@Body request: LoginRequest): Call<LoginResponse>

    // DELETE /v1/usuario/:id - Sem mudança
    @DELETE("usuario/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>
}