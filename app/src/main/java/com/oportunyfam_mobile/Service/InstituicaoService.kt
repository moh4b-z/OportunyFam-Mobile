package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile_ong.model.AlunosResponse
import com.oportunyfam_mobile_ong.model.InstituicaoAtualizarRequest
import com.oportunyfam_mobile_ong.model.InstituicaoRequest
import com.oportunyfam_mobile_ong.model.InstituicaoResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface InstituicaoService {

    // POST - Criar nova instituição
    @Headers("Content-Type: application/json")
    @POST("instituicoes")
    suspend fun criar(@Body request: InstituicaoRequest): Response<InstituicaoResponse>


    // GET - Listar todas as instituições
    @GET("instituicoes")
    fun listarTodas(): Call<List<InstituicaoResponse>>

    // GET - Buscar instituição por ID
    @GET("instituicoes/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<InstituicaoResponse>

    // PUT - Atualizar instituição por ID - ADICIONANDO HEADER EXPLÍCITO
    @Headers("Content-Type: application/json")
    @PUT("instituicoes/{id}")
    suspend fun atualizar(@Path("id") id: Int, @Body request: InstituicaoAtualizarRequest): Response<InstituicaoResponse>

    // DELETE - Excluir instituição por ID
    @DELETE("instituicoes/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>

    // GET - Busca com filtros e paginação
    @GET("instituicoes")
    fun buscarComFiltro(
        @Query("busca") termo: String?,
        @Query("pagina") pagina: Int?,
        @Query("tamanho") tamanho: Int?
    ): Call<Any>

    // GET - Busca com filtros e paginação
    @GET("instituicoes/alunos/")
    fun buscarAlunos(
        @Query("instituicao_id") instituicao_id: Int?,
        @Query("atividade_id") atividade_id: Int?,
        @Query("status_id") status_id: Int?
    ): Call<AlunosResponse>
}