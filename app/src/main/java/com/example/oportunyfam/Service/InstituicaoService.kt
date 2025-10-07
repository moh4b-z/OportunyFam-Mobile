package com.example.oportunyfam.Service

import com.example.oportunyfam.model.Instituicao
import retrofit2.Call
import retrofit2.http.*

interface InstituicaoService {

    // POST - Criar nova instituição
    @POST("instituicoes")
    fun criar(@Body instituicao: Instituicao): Call<Instituicao>

    // GET - Listar todas as instituições
    // Esta rota é a mesma usada para a busca/filtro
    @GET("instituicoes")
    fun listarTodas(): Call<List<Instituicao>>

    // GET - Buscar instituição por ID
    @GET("instituicoes/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<Instituicao>

    // PUT - Atualizar instituição por ID
    // O backend usa PUT /:id, então o ID pode vir do objeto e/ou do Path
    @PUT("instituicoes/{id}")
    fun atualizar(@Path("id") id: Int, @Body instituicao: Instituicao): Call<Instituicao>

    // DELETE - Excluir instituição por ID
    // Assumindo que o delete recebe o ID no Path
    @DELETE("instituicoes/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>

    // GET - Busca com filtros e paginação (usando a Stored Procedure)
    // Ex: GET /v1/instituicoes?busca=escola&pagina=1
    @GET("instituicoes")
    fun buscarComFiltro(
        @Query("busca") termo: String?,
        @Query("pagina") pagina: Int?,
        @Query("tamanho") tamanho: Int?
    ): Call<Any> // O Call<Any> é usado pois o retorno é complexo (metadata + instituições)
}