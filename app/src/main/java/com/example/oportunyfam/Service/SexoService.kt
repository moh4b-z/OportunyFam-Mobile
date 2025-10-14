package com.example.oportunyfam.Service

import com.example.oportunyfam.model.SexosResponse
import retrofit2.Call
import retrofit2.http.*

interface SexoService {

    @GET("sexos")
    fun listarTodos(): Call<SexosResponse>
}