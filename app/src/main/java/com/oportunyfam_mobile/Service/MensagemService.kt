package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.MensagemAtualizarRequest
import com.oportunyfam_mobile.model.MensagemRequest
import com.oportunyfam_mobile.model.MensagemResponse
import com.oportunyfam_mobile.model.MensagensResponse
import retrofit2.Response
import retrofit2.http.*

interface MensagemService {

    // POST - Criar nova mensagem
    @Headers("Content-Type: application/json")
    @POST("conversas/mensagens")
    suspend fun criar(@Body request: MensagemRequest): Response<MensagemResponse>

    // GET - Listar todas as mensagens
    @GET("conversas/mensagens")
    suspend fun listarTodas(): Response<MensagensResponse>

    // GET - Listar todas as mensagens de uma conversa específica
    @GET("conversas/{id}/mensagens")
    suspend fun listarPorConversa(@Path("id") idConversa: Int): Response<MensagensResponse>

    // GET - Buscar mensagem por ID
    @GET("conversas/mensagens/{id}")
    suspend fun buscarPorId(@Path("id") id: Int): Response<MensagemResponse>

    // PUT - Atualizar mensagem por ID (para marcar como visto ou editar descrição)
    @Headers("Content-Type: application/json")
    @PUT("conversas/mensagens/{id}")
    suspend fun atualizar(
        @Path("id") id: Int,
        @Body request: MensagemAtualizarRequest
    ): Response<MensagemResponse>

    // DELETE - Excluir mensagem por ID
    @DELETE("conversas/mensagens/{id}")
    suspend fun deletar(@Path("id") id: Int): Response<MensagemResponse>
}
