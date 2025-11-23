package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.UsuarioRequest
import com.oportunyfam_mobile.model.UsuarioResponse
import retrofit2.Call
import retrofit2.http.*

interface UsuarioService {

    // POST /v1/usuario - Envia o Request, retorna o Response
    @Headers("Content-Type: application/json")
    @POST("usuarios")
    fun criar(@Body usuario: UsuarioRequest): Call<UsuarioResponse>

    // PUT /v1/usuario/:id - Envia campos parciais como um mapa (o backend aceita atualizar apenas os atributos enviados)
    @Headers("Content-Type: application/json")
    @PUT("usuarios/{id}")
    fun atualizar(@Path("id") id: Int, @Body fields: Map<String, @JvmSuppressWildcards Any?>): Call<UsuarioResponse>

    // GET /v1/usuario - Retorna Lista de Responses
    @GET("usuarios")
    fun listarTodos(): Call<List<UsuarioResponse>>

    // GET /v1/usuario/:id - Retorna um Response
    @GET("usuarios/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<UsuarioResponse>

    // DELETE /v1/usuario/:id - Sem mudança
    @DELETE("usuarios/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>
}