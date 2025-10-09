package com.example.oportunyfam.Service
import com.example.oportunyfam.model.Oportunidade
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
interface OportunidadeService {

    @GET("oportunidades")
    suspend fun getOportunidades(): List<Oportunidade>

    @POST("oportunidades/{id}/selecionar")
    suspend fun selecionarOportunidade(@Path("id") id: Int)

    companion object
}