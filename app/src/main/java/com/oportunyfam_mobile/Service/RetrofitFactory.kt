package com.oportunyfam_mobile.Service

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import com.oportunyfam_mobile.model.ResultData // Importa ResultData

class RetrofitFactory {
    private val BASE_URL = "http://192.168.15.14:8080/v1/oportunyfam/"

    // O objeto Gson (LocalDateAdapter não está incluído aqui para manter o foco)
    private val gson = GsonBuilder()
        .setLenient()
        // Supondo que LocalDateAdapter existe e está registrado aqui:
        // .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        // ** NOVO: Registra o adaptador customizado para a sealed class ResultData **
        .registerTypeAdapter(ResultData::class.java, ResultDataTypeAdapter())
        .create()

    // 1. Cria o interceptor de logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Define o nível de detalhe (Headers e Body serão logados no Logcat)
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. Adiciona o interceptor ao cliente OkHttp
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Adiciona o interceptor
        .build()

    private val retrofitFactory = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        // Passa o objeto 'gson' configurado
        .addConverterFactory(GsonConverterFactory.create(gson))
        // Conecta o cliente que contém o interceptor
        .client(client)
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
