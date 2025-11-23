package com.oportunyfam_mobile.Service

import com.google.gson.GsonBuilder
import com.oportunyfam_mobile.model.LoginResponse
import com.oportunyfam_mobile.model.LoginResponseDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitFactory {

    companion object {
        // URL base da API
        private const val BASE_URL = "https://oportunyfam-bcf0ghd9fkevaeez.canadacentral-01.azurewebsites.net/v1/oportunyfam/"

        // Níveis de log
        private val LOG_LEVEL = HttpLoggingInterceptor.Level.BODY

        // Configuração do RetryInterceptor (podemos expor esses valores se precisar ajustar em runtime)
        private const val RETRY_MAX_ATTEMPTS = 4
        private const val RETRY_INITIAL_DELAY_MS = 1500L
        private const val RETRY_MAX_DELAY_MS = 10000L
    }

    /**
     * Configuração do Gson com deserializador customizado
     */
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(LoginResponse::class.java, LoginResponseDeserializer())
        .create()

    /**
     * Interceptor para logging de requisições e respostas
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = LOG_LEVEL
    }

    /**
     * Cliente HTTP com configurações personalizadas
     */
    private val client = OkHttpClient.Builder()
        // aumente os timeouts para lidar com alta latência do servidor
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS)
        // permitir tentativas automáticas em falhas de conexão
        .retryOnConnectionFailure(true)
        // nosso interceptor de retry com backoff exponencial e jitter
        .addInterceptor(RetryInterceptor(RETRY_MAX_ATTEMPTS, RETRY_INITIAL_DELAY_MS, RETRY_MAX_DELAY_MS))
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestWithHeader = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
            chain.proceed(requestWithHeader)
        }
        .build()

    /**
     * Instância principal do Retrofit
     */
    private val retrofitFactory = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    // ==================== SERVIÇOS ====================

    /**
     * Serviço de Instituições
     */
    fun getInstituicaoService(): InstituicaoService {
        return retrofitFactory.create(InstituicaoService::class.java)
    }

    /**
     * Serviço de Usuários
     */
    fun getUsuarioService(): UsuarioService {
        return retrofitFactory.create(UsuarioService::class.java)
    }

    /**
     * Serviço de Endereços
     */
    fun getEnderecoService(): EnderecoService {
        return retrofitFactory.create(EnderecoService::class.java)
    }

    /**
     * Serviço de Atividades
     */
    fun getAtividadeService(): AtividadeService {
        return retrofitFactory.create(AtividadeService::class.java)
    }

    /**
     * Serviço de Tipos de Instituição
     */
    fun getTipoInstituicaoService(): TipoInstituicaoService {
        return retrofitFactory.create(TipoInstituicaoService::class.java)
    }

    /**
     * Serviço de Crianças
     */
    fun getCriancaService(): CriancaService {
        return retrofitFactory.create(CriancaService::class.java)
    }

    /**
     * Serviço de Login Universal
     */
    fun getLoginUniversalService(): LoginUniversalService {
        return retrofitFactory.create(LoginUniversalService::class.java)
    }

    /**
     * Serviço de Publicações
     */
    fun getPublicacaoService(): PublicacaoService {
        return retrofitFactory.create(PublicacaoService::class.java)
    }

    /**
     * Serviço de Mensagens
     */
    fun getMensagemService(): MensagemService {
        return retrofitFactory.create(MensagemService::class.java)
    }

    /**
     * Serviço de Conversas
     */
    fun getConversaService(): ConversaService {
        return retrofitFactory.create(ConversaService::class.java)
    }

    fun getMatriculaService(): MatriculaService {
        return retrofitFactory.create(MatriculaService::class.java)
    }

    fun getInscricaoService(): InscricaoService {
        return retrofitFactory.create(InscricaoService::class.java)
    }

    fun getCategoriaService(): CategoriaService {
        return retrofitFactory.create(CategoriaService::class.java)
    }

    fun getSexoService(): SexoService {
        return retrofitFactory.create(SexoService::class.java)
    }

}