package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.PublicacaoCriadaResponse
import com.oportunyfam_mobile.model.PublicacaoRequest
import com.oportunyfam_mobile.model.PublicacoesListResponse
import retrofit2.Call
import retrofit2.http.*

interface PublicacaoService {

    // GET /publicacaoInstituicoes - Buscar todas as publicações
    @GET("publicacaoInstituicoes")
    fun buscarTodasPublicacoes(): Call<PublicacoesListResponse>

    // GET /publicacaoInstituicoes/instituicao/:idInstituicao - Buscar publicações por instituição
    @GET("publicacaoInstituicoes/instituicao/{idInstituicao}")
    fun buscarPublicacoesPorInstituicao(@Path("idInstituicao") idInstituicao: Int): Call<PublicacoesListResponse>

    // GET /publicacaoInstituicoes/:id - Buscar publicação por ID
    @GET("publicacaoInstituicoes/{id}")
    fun buscarPublicacaoPorId(@Path("id") id: Int): Call<PublicacaoCriadaResponse>

    // POST /publicacaoInstituicoes - Criar publicação
    @Headers("Content-Type: application/json")
    @POST("publicacaoInstituicoes")
    fun criarPublicacao(@Body publicacao: PublicacaoRequest): Call<PublicacaoCriadaResponse>

    // PUT /publicacaoInstituicoes/:id - Atualizar publicação
    @Headers("Content-Type: application/json")
    @PUT("publicacaoInstituicoes/{id}")
    fun atualizarPublicacao(@Path("id") id: Int, @Body publicacao: PublicacaoRequest): Call<PublicacaoCriadaResponse>

    // DELETE /publicacaoInstituicoes/:id - Deletar publicação
    @DELETE("publicacaoInstituicoes/{id}")
    fun deletarPublicacao(@Path("id") id: Int): Call<Unit>
}

