package com.oportunyfam_mobile_ong.Service

import com.oportunyfam_mobile_ong.model.*
import retrofit2.Call
import retrofit2.http.*

interface InscricaoService {

    // POST /inscricoes - Criar inscrição
    @Headers("Content-Type: application/json")
    @POST("inscricoes")
    fun criarInscricao(@Body inscricao: InscricaoRequest): Call<InscricaoCriadaResponse>

    // GET /inscricoes - Buscar todas as inscrições
    @GET("inscricoes")
    fun buscarTodasInscricoes(): Call<InscricoesResponse>

    // GET /inscricoes/:id - Buscar inscrição por ID
    @GET("inscricoes/{id}")
    fun buscarInscricaoPorId(@Path("id") id: Int): Call<InscricaoUnicaResponse>

    // GET /inscricoes/crianca/:idCrianca - Buscar inscrições por ID da criança
    @GET("inscricoes/crianca/{idCrianca}")
    fun buscarInscricoesPorCrianca(@Path("idCrianca") idCrianca: Int): Call<InscricoesResponse>

    // PUT /inscricoes/:id - Atualizar inscrição (status, observação)
    @Headers("Content-Type: application/json")
    @PUT("inscricoes/{id}")
    fun atualizarInscricao(@Path("id") id: Int, @Body inscricao: InscricaoUpdateRequest): Call<InscricaoCriadaResponse>

    // DELETE /inscricoes/:id - Deletar inscrição
    @DELETE("inscricoes/{id}")
    fun deletarInscricao(@Path("id") id: Int): Call<Unit>
}

