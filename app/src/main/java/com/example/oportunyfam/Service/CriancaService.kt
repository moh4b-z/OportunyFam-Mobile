package com.example.oportunyfam.Service

import com.example.oportunyfam.model.CriancaRequest
import com.example.oportunyfam.model.CriancaResponse
import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam.model.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface CriancaService {

    @POST("crianca")
    fun criar(@Body crianca: CriancaRequest): Call<CriancaResponse>

    @PUT("crianca/{id}")
    fun atualizar(@Path("id") id: Int, @Body crianca: CriancaRequest): Call<CriancaResponse>

    @GET("crianca")
    fun listarTodas(): Call<List<CriancaResponse>>

    @GET("crianca/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<CriancaResponse>

    @POST("crianca/login")
    fun loginCrianca(@Body request: LoginRequest): Call<LoginResponse>

    @DELETE("crianca/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>
}