package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.MatriculaRequest
import com.oportunyfam_mobile.model.MatriculaResponse
import com.oportunyfam_mobile.model.MatriculaUpdateRequest
import com.oportunyfam_mobile.model.MatriculasResponse
import retrofit2.Call
import retrofit2.http.*

interface MatriculaService {

    // POST /matriculas - Criar matrícula (registrar presença/ausência)
    @Headers("Content-Type: application/json")
    @POST("matriculas")
    fun criarMatricula(@Body matricula: MatriculaRequest): Call<MatriculaResponse>

    // GET /matriculas - Buscar todas as matrículas
    @GET("matriculas")
    fun buscarTodasMatriculas(): Call<MatriculasResponse>

    // GET /matriculas/:id - Buscar matrícula por ID
    @GET("matriculas/{id}")
    fun buscarMatriculaPorId(@Path("id") id: Int): Call<MatriculaResponse>

    // PUT /matriculas/:id - Atualizar matrícula (presente/ausente, observação)
    @Headers("Content-Type: application/json")
    @PUT("matriculas/{id}")
    fun atualizarMatricula(@Path("id") id: Int, @Body matricula: MatriculaUpdateRequest): Call<MatriculaResponse>

    // DELETE /matriculas/:id - Deletar matrícula
    @DELETE("matriculas/{id}")
    fun deletarMatricula(@Path("id") id: Int): Call<Unit>
}

