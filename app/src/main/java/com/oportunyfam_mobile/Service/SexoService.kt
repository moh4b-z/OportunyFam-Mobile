package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.SexosResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface SexoService {

    @GET("sexos")
    suspend fun listarTodos(): Response<SexosResponse>
}