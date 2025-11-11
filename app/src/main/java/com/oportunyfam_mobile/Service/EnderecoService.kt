package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile_ong.model.EnderecoRequest
import com.oportunyfam_mobile_ong.model.EnderecoResponse
import com.oportunyfam_mobile_ong.model.OSMResultado
import retrofit2.Call
import retrofit2.http.*

interface EnderecoService {

    // POST /v1/endereco - Criar novo endereço
    @POST("endereco")
    fun criar(@Body endereco: EnderecoRequest): Call<EnderecoResponse>

    // DELETE /v1/endereco/:id - Excluir endereço por ID
    @DELETE("endereco/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>

    // PUT /v1/endereco/:id - Atualizar endereço por ID
    @PUT("endereco/{id}")
    fun atualizar(@Path("id") id: Int, @Body endereco: EnderecoRequest): Call<EnderecoResponse>

    // GET /v1/endereco - Buscar todos os endereços (ou com filtros simples)
    @GET("endereco")
    fun listarTodos(): Call<List<EnderecoResponse>>

    // GET /v1/endereco/:id - Buscar endereço por ID
    @GET("endereco/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<EnderecoResponse>

    // GET /v1/endereco/osm - Buscar Instituições próximas usando OpenStreetMap
    // A rota no backend usa Query Parameters: /osm/?lat=X&lng=Y&tipo=school&raio=10
    @GET("endereco/osm")
    fun buscarInstituicoesProximas(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("tipo") tipo: String = "school", // Default school, se não for passado
        @Query("raio") raio: Int = 1000 // Raio em metros
    ): Call<List<OSMResultado>>
}