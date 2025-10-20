package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.CriancaRequest
import com.oportunyfam_mobile.model.CriancaResponse
import com.oportunyfam_mobile.model.LoginRequest
import com.oportunyfam_mobile.model.LoginResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CriancaService {
    @Headers("Content-Type: application/json")
    @POST("crianca")
    suspend fun criar(@Body crianca: CriancaRequest): Response<CriancaResponse>

    @PUT("crianca/{id}")
    suspend fun atualizar(@Path("id") id: Int, @Body crianca: CriancaRequest): Response<CriancaResponse>

    @GET("crianca")
    suspend fun listarTodas(): Response<CriancaResponse>

    @GET("crianca/{id}")
    suspend fun buscarPorId(@Path("id") id: Int): Response<CriancaResponse>

    @POST("crianca/login")
    suspend fun loginCrianca(@Body request: LoginRequest): Response<LoginResponse>

    @DELETE("crianca/{id}")
    suspend fun deletar(@Path("id") id: Int): Response<Unit>
}