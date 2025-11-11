package com.oportunyfam_mobile_ong.Service

import com.oportunyfam_mobile_ong.model.TipoInstituicao
import com.oportunyfam_mobile_ong.model.TipoInstituicaoResponse
import retrofit2.Call
import retrofit2.http.*

interface TipoInstituicaoService {

    // POST /v1/tipoInstituicao - Criar novo tipo
    @POST("tipoInstituicoes")
    fun criar(@Body tipoInstituicao: TipoInstituicao): Call<TipoInstituicao>

    // DELETE /v1/tipoInstituicao/:id - Excluir tipo por ID
    @DELETE("tipoInstituicoes/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>

    // PUT /v1/tipoInstituicao/:id - Atualizar tipo por ID
    @PUT("tipoInstituicoes/{id}")
    fun atualizar(@Path("id") id: Int, @Body tipoInstituicao: TipoInstituicao): Call<TipoInstituicao>

    // GET /v1/tipoInstituicao - Listar todos os tipos
    @GET("tipoInstituicoes")
    fun listarTodos(): Call<TipoInstituicaoResponse>

    // GET /v1/tipoInstituicao/:id - Buscar tipo por ID
    @GET("tipoInstituicoes/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<TipoInstituicaoResponse>


}