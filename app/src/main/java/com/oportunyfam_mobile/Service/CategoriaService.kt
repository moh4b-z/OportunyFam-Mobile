package com.oportunyfam_mobile_ong.Service

import com.oportunyfam_mobile_ong.model.*
import retrofit2.Call
import retrofit2.http.*

interface CategoriaService {

    // POST /categorias - Criar categoria
    @Headers("Content-Type: application/json")
    @POST("categorias")
    fun criarCategoria(@Body categoria: CategoriaRequest): Call<CategoriaResponse>

    // GET /categorias - Buscar todas as categorias
    @GET("categorias")
    fun buscarTodasCategorias(): Call<CategoriasResponse>

    // GET /categorias/:id - Buscar categoria por ID
    @GET("categorias/{id}")
    fun buscarCategoriaPorId(@Path("id") id: Int): Call<CategoriaResponse>

    // PUT /categorias/:id - Atualizar categoria
    @Headers("Content-Type: application/json")
    @PUT("categorias/{id}")
    fun atualizarCategoria(@Path("id") id: Int, @Body categoria: CategoriaRequest): Call<CategoriaResponse>

    // DELETE /categorias/:id - Deletar categoria
    @DELETE("categorias/{id}")
    fun deletarCategoria(@Path("id") id: Int): Call<Unit>
}

