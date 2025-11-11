package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.ConversaRequest
import com.oportunyfam_mobile.model.ConversaResponse
import com.oportunyfam_mobile.model.ConversasResponse
import com.oportunyfam_mobile.model.ConversasPessoaResponse
import retrofit2.Response
import retrofit2.http.*

interface ConversaService {

    // POST - Criar nova conversa
    @Headers("Content-Type: application/json")
    @POST("conversas")
    suspend fun criar(@Body request: ConversaRequest): Response<ConversaResponse>

    // GET - Listar todas as conversas
    @GET("conversas")
    suspend fun listarTodas(): Response<ConversasResponse>

    // GET - Buscar conversas por ID da pessoa (retorna formato com outro_participante)
    @GET("conversas/pessoa/{id}")
    suspend fun buscarPorIdPessoa(@Path("id") id: Int): Response<ConversasPessoaResponse>

    // GET - Buscar conversa por ID
    @GET("conversas/{id}")
    suspend fun buscarPorId(@Path("id") id: Int): Response<ConversaResponse>

    // DELETE - Excluir conversa por ID
    @DELETE("conversas/{id}")
    suspend fun deletar(@Path("id") id: Int): Response<ConversaResponse>

    // PUT - Atualizar conversa por ID (se necessário)
    @Headers("Content-Type: application/json")
    @PUT("conversas/{id}")
    suspend fun atualizar(
        @Path("id") id: Int,
        @Body request: ConversaRequest
    ): Response<ConversaResponse>
}
