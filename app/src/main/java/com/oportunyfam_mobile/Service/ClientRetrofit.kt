package com.oportunyfam_mobile.Service


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://localhost:8080/v1/oportunyfam/instituicoes/?nome=&pagina=1&tamanho=20" // Troque pelo seu backend

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instituicaoService: InstituicaoService by lazy {
        instance.create(InstituicaoService::class.java)
    }
}
