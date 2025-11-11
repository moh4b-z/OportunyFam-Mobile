package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.AtividadeCriadaResponse
import com.oportunyfam_mobile.model.AtividadeRequest
import com.oportunyfam_mobile.model.AtividadeUnicaResponse
import com.oportunyfam_mobile.model.AtividadesListResponse
import com.oportunyfam_mobile.model.AulaCriadaResponse
import com.oportunyfam_mobile.model.AulaLoteRequest
import com.oportunyfam_mobile.model.AulaLoteResponse
import com.oportunyfam_mobile.model.AulaRequest
import com.oportunyfam_mobile.model.AulaUnicaResponse
import com.oportunyfam_mobile.model.AulasListResponse
import retrofit2.Call
import retrofit2.http.*

interface AtividadeService {

    // ============ ROTAS DE ATIVIDADE ============

    // POST /atividades - Criar atividade
    @Headers("Content-Type: application/json")
    @POST("atividades")
    fun criarAtividade(@Body atividade: AtividadeRequest): Call<AtividadeCriadaResponse>

    // GET /atividades - Buscar todas as atividades
    @GET("atividades")
    fun buscarTodasAtividades(): Call<AtividadesListResponse>

    // GET /atividades/:id - Buscar atividade por ID
    @GET("atividades/{id}")
    fun buscarAtividadePorId(@Path("id") id: Int): Call<AtividadeUnicaResponse>

    // GET /atividades/instituicao/:idInstituicao - Buscar atividades por instituição
    @GET("atividades/instituicao/{idInstituicao}")
    fun buscarAtividadesPorInstituicao(@Path("idInstituicao") idInstituicao: Int): Call<AtividadesListResponse>

    // PUT /atividades/:id - Atualizar atividade
    @Headers("Content-Type: application/json")
    @PUT("atividades/{id}")
    fun atualizarAtividade(@Path("id") id: Int, @Body atividade: AtividadeRequest): Call<AtividadeCriadaResponse>

    // DELETE /atividades/:id - Deletar atividade
    @DELETE("atividades/{id}")
    fun deletarAtividade(@Path("id") id: Int): Call<Unit>

    // ============ ROTAS DE AULAS ============

    // POST /atividades/aulas - Criar uma aula
    @Headers("Content-Type: application/json")
    @POST("atividades/aulas")
    fun criarAula(@Body aula: AulaRequest): Call<AulaCriadaResponse>

    // POST /atividades/aulas/lote - Criar várias aulas de uma vez
    @Headers("Content-Type: application/json")
    @POST("atividades/aulas/lote")
    fun criarAulasLote(@Body aulaLote: AulaLoteRequest): Call<AulaLoteResponse>

    // GET /atividades/aulas - Buscar todas as aulas
    @GET("atividades/aulas")
    fun buscarTodasAulas(): Call<AulasListResponse>

    // GET /atividades/aulas/:id - Buscar aula por ID
    @GET("atividades/aulas/{id}")
    fun buscarAulaPorId(@Path("id") id: Int): Call<AulaUnicaResponse>

    // GET /atividades/aulas/instituicao/:idInstituicao - Buscar todas as aulas de uma instituição
    @GET("atividades/aulas/instituicao/{idInstituicao}")
    fun buscarAulasPorInstituicao(@Path("idInstituicao") idInstituicao: Int): Call<AulasListResponse>

    // PUT /atividades/aulas/:id - Atualizar aula
    @Headers("Content-Type: application/json")
    @PUT("atividades/aulas/{id}")
    fun atualizarAula(@Path("id") id: Int, @Body aula: AulaRequest): Call<AulaCriadaResponse>

    // DELETE /atividades/aulas/:id - Deletar aula
    @DELETE("atividades/aulas/{id}")
    fun deletarAula(@Path("id") id: Int): Call<Unit>
}