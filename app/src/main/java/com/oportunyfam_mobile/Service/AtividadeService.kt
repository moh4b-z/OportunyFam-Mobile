package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.AtividadeRequest
import com.oportunyfam_mobile.model.AtividadeResponse
import com.oportunyfam_mobile.model.AulaRequest
import com.oportunyfam_mobile.model.AulaResponse
import retrofit2.Call
import retrofit2.http.*

interface AtividadeService {

    @POST("atividades")
    fun criarAtividade(@Body atividade: AtividadeRequest): Call<AtividadeResponse>

    @PUT("atividades/{id}")
    fun atualizarAtividade(@Path("id") id: Int, @Body atividade: AtividadeRequest): Call<AtividadeResponse>

    @DELETE("atividades/{id}")
    fun deletarAtividade(@Path("id") id: Int): Call<Unit>

    @GET("atividades")
    fun buscarTodasAtividades(): Call<List<AtividadeResponse>> // Retorna detalhes da View

    @GET("atividades/{id}")
    fun buscarAtividadePorId(@Path("id") id: Int): Call<AtividadeResponse>

    // --- ROTAS DE AULAS (CRUD) ---
    // Nota: O prefixo "/aulas" é adicionado à URL base do Retrofit, se a rota for absoluta.
    // Se a rota for relativa (como aqui), ela é concatenada com o prefixo do controller.

    // POST /v1/atividades/aulas/
    @POST("atividades/aulas")
    fun criarAula(@Body aula: AulaRequest): Call<AulaResponse>

    // PUT /v1/atividades/aulas/:id
    @PUT("atividades/aulas/{id}")
    fun atualizarAula(@Path("id") id: Int, @Body aula: AulaRequest): Call<AulaResponse>

    // DELETE /v1/atividades/aulas/:id
    @DELETE("atividades/aulas/{id}")
    fun deletarAula(@Path("id") id: Int): Call<Unit>

    // GET /v1/atividades/aulas/:id (Buscar aula específica)
    @GET("atividades/aulas/{id}")
    fun buscarAulaPorId(@Path("id") id: Int): Call<AulaResponse> // Usamos AulaResponse aqui, não o HorarioDetalhe

    // GET /v1/atividades/aulas (Buscar todas as aulas)
    @GET("atividades/aulas")
    fun buscarTodasAulas(): Call<List<AulaResponse>>

    // GET /v1/atividades/aulas/instituicao/:idInstituicao (Buscar aulas por Instituição)
    @GET("atividades/aulas/instituicao/{idInstituicao}")
    fun buscarAulasPorInstituicao(@Path("idInstituicao") idInstituicao: Int): Call<List<AulaResponse>>
}