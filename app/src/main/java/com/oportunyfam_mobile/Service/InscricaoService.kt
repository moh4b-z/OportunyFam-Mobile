package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.InscricaoCriadaResponse
import com.oportunyfam_mobile.model.InscricaoRequest
import com.oportunyfam_mobile.model.InscricaoUnicaResponse
import com.oportunyfam_mobile.model.InscricaoUpdateRequest
import com.oportunyfam_mobile.model.InscricoesResponse
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

    // GET /inscricoes/atividade/:idAtividade - Buscar inscrições por ID da atividade
    @GET("inscricoes/atividade/{idAtividade}")
    fun buscarInscricoesPorAtividade(@Path("idAtividade") idAtividade: Int): Call<InscricoesResponse>

    // PUT /inscricoes/:id - Atualizar inscrição (status, observação)
    @Headers("Content-Type: application/json")
    @PUT("inscricoes/{id}")
    fun atualizarInscricao(@Path("id") id: Int, @Body inscricao: InscricaoUpdateRequest): Call<InscricaoCriadaResponse>

    // DELETE /inscricoes/:id - Deletar inscrição
    @DELETE("inscricoes/{id}")
    fun deletarInscricao(@Path("id") id: Int): Call<Unit>
}

