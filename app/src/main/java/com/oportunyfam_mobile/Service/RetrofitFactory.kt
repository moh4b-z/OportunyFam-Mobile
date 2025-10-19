package com.oportunyfam_mobile.Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {
    private val BASE_URL = "http://10.0.2.2:8080/v1/oportunyfam/"

    private val retrofitFactory = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    fun getInstituicaoService(): InstituicaoService {
        return retrofitFactory.create(InstituicaoService::class.java)
    }

    fun getSexoService(): SexoService {
        return retrofitFactory.create(SexoService::class.java)
    }

    fun getLoginUniversalService(): LoginUniversalService {
        return retrofitFactory.create(LoginUniversalService::class.java)
    }

    fun getUsuarioService(): UsuarioService {
        return retrofitFactory.create(UsuarioService::class.java)
    }
    fun getEnderecoService(): EnderecoService {
        return retrofitFactory.create(EnderecoService::class.java)
    }
    fun getAtividadeService(): AtividadeService {
        return retrofitFactory.create(AtividadeService::class.java)
    }
}