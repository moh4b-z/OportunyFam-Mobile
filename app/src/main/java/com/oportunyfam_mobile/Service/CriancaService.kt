package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.CriancaListResponse
import com.oportunyfam_mobile.model.CriancaRequest
import com.oportunyfam_mobile.model.CriancaResponse
import com.oportunyfam_mobile.model.LoginRequest
import com.oportunyfam_mobile.model.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface CriancaService {

    // POST /v1/oportunyfam/criancas
    @POST("oportunyfam/criancas")
    fun criar(@Body crianca: CriancaRequest): Call<CriancaResponse>

    // PUT /v1/oportunyfam/criancas/:id
    @PUT("oportunyfam/criancas/{id}")
    fun atualizar(@Path("id") id: Int, @Body crianca: CriancaRequest): Call<CriancaResponse>

    // GET /v1/oportunyfam/criancas
    @GET("oportunyfam/criancas")
    fun listarTodas(): Call<CriancaListResponse>

    // GET /v1/oportunyfam/usuarios/:usuarioId/criancas
    @GET("oportunyfam/usuarios/{usuarioId}/criancas")
    fun listarPorUsuario(@Path("usuarioId") usuarioId: Int): Call<CriancaListResponse>

    // GET /v1/oportunyfam/criancas/:id
    @GET("oportunyfam/criancas/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<CriancaResponse>

    // POST /v1/oportunyfam/criancas/login
    @POST("oportunyfam/criancas/login")
    fun loginCrianca(@Body request: LoginRequest): Call<LoginResponse>

    // DELETE /v1/oportunyfam/criancas/:id
    @DELETE("oportunyfam/criancas/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>
}
