package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile_ong.model.LoginRequest
import com.oportunyfam_mobile_ong.model.LoginResponse
import com.oportunyfam_mobile.model.UsuarioRequest
import com.oportunyfam_mobile.model.UsuarioResponse
import retrofit2.Call
import retrofit2.http.*

interface UsuarioService {

    // POST /v1/usuario - Envia o Request, retorna o Response
    @Headers("Content-Type: application/json")
    @POST("usuario")
    fun criar(@Body usuario: UsuarioRequest): Call<UsuarioResponse>

    // PUT /v1/usuario/:id - Envia o Request, retorna o Response
    @Headers("Content-Type: application/json")
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