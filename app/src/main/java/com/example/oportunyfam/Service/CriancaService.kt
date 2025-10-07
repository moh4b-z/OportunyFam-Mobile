package com.example.oportunyfam.Service

import com.example.oportunyfam.model.CriancaRequest
import com.example.oportunyfam.model.CriancaResponse
import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam.model.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface CriancaService {

    // POST /v1/crianca - Envia o Request, retorna o Response
    @POST("crianca")
    fun criar(@Body crianca: CriancaRequest): Call<CriancaResponse>

    // PUT /v1/crianca/:id - Envia o Request, retorna o Response
    @PUT("crianca/{id}")
    fun atualizar(@Path("id") id: Int, @Body crianca: CriancaRequest): Call<CriancaResponse>

    // GET /v1/crianca - Retorna Lista de Responses
    @GET("crianca")
    fun listarTodas(): Call<List<CriancaResponse>>

    // GET /v1/crianca/:id - Retorna um Response
    @GET("crianca/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<CriancaResponse>

    // POST /v1/crianca/login - Sem mudança
    @POST("crianca/login")
    fun loginCrianca(@Body request: LoginRequest): Call<LoginResponse>

    // DELETE /v1/crianca/:id - Sem mudança
    @DELETE("crianca/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>
}