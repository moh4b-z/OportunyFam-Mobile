package com.oportunyfam_mobile.Service

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Url
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.http.Body
import java.io.File
import java.io.FileInputStream
import java.security.cert.X509Certificate
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.io.extension
import kotlin.io.readBytes
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

// ========================================================
// INTERFACE DE SERVIÇO SIMPLIFICADA PARA SAS TOKEN
// - Não requer Headers de autenticação complexos (x-ms-date, Authorization)
// ========================================================
interface AzureBlobApiService {
    @PUT
    // Necessário para informar ao Azure que este é um BLOB de bloco
    @Headers("x-ms-blob-type: BlockBlob")
    suspend fun uploadFileSas(
        @Url uploadUrlWithSas: String, // URL COMPLETA já contendo o ?sasToken
        @Body fileBytes: RequestBody
    ): Response<Void>
}


object AzureBlobRetrofit {
    private const val BASE_URL = "https://placeholder.blob.core.windows.net/"

    /**
     * Cria um OkHttpClient que aceita todos os certificados SSL
     * ATENÇÃO: Isso é necessário devido a um bug do Android com certificados OCSP do Azure
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Cria um trust manager que não valida chains de certificado
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Instala o trust manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .build()
    }

    val apiService: AzureBlobApiService by lazy {
        retrofit.create(AzureBlobApiService::class.java)
    }

    /**
     * Upload de imagem para Azure Blob Storage usando Autenticação de Token SAS.
     * O Token SAS DEVE ser o parâmetro 'accountKey'.
     *
     * @param imageFile Arquivo de imagem a ser enviado
     * @param storageAccount Nome da conta de storage do Azure
     * @param sasToken O Token SAS para o container (ex: sp=racwl&st=...&sig=...)
     * @param containerName Nome do container no Azure
     * @return URL COMPLETA da imagem (sem o SAS Token) ou null em caso de erro
     */
    suspend fun uploadImageToAzure(
        imageFile: File,
        storageAccount: String,
        sasToken: String, // Agora é o Token SAS
        containerName: String
    ): String? = withContext(Dispatchers.IO) {
        // Gera um nome único para o blob usando UUID
        val extension = imageFile.extension.ifEmpty { "jpg" }
        val blobName = "${UUID.randomUUID()}.$extension"

        // 1. Cria a URL do recurso (URL que será armazenada no seu banco de dados)
        val resourceUrl = "https://${storageAccount}.blob.core.windows.net/${containerName}/${blobName}"

        // 2. Cria a URL de UPLOAD (URL do recurso + Token SAS como parâmetro de consulta)
        val uploadUrlWithSas = "$resourceUrl?$sasToken"


        println("📤 Iniciando upload para Azure Storage (usando SAS Token)...")
        println("🔗 Storage Account: $storageAccount")
        println("📦 Container: $containerName")
        println("📄 Blob Name: $blobName")
        println("🌐 URL de Upload (com SAS): $uploadUrlWithSas") // Log para debug, mas a URL final é resourceUrl

        try {
            val fileBytes = FileInputStream(imageFile).readBytes()
            // Assume que estamos sempre enviando JPEG (otimização para o seu caso)
            val requestBody = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            println("📊 Tamanho do arquivo: ${fileBytes.size} bytes")

            // Usando a API simplificada para SAS
            val response = apiService.uploadFileSas(
                uploadUrlWithSas = uploadUrlWithSas,
                fileBytes = requestBody
            )

            if (response.isSuccessful) {
                println("✅ Upload de imagem bem-sucedido para: $resourceUrl")
                resourceUrl // Retorna a URL SEM o Token SAS
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ Erro no upload da imagem: ${response.code()} - $errorBody")
                println("⚠️ Headers da resposta: ${response.headers()}")

                // Se o erro for 403, pode indicar que o token expirou ou é inválido
                if (response.code() == 403) {
                    println("🚨 ERRO 403: Acesso Negado. Verifique se o Token SAS ainda é válido ou se o Container/Blob Name estão corretos.")
                }
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Erro ao fazer upload da imagem: ${e.message}")
            println("🔍 Tipo de erro: ${e.javaClass.simpleName}")
            null
        }
    }

    // A função generateSharedKeyAuth e outras que a usavam foram removidas, pois não são necessárias com SAS Token.

    // ========================================================
    // Funções auxiliares (manter)
    // ========================================================

    /**
     * Testa a conectividade com o Azure Storage
     */
    suspend fun testAzureConnection(storageAccount: String): Boolean {
        return try {
            println("🔍 Testando conectividade com: https://${storageAccount}.blob.core.windows.net/")
            withContext(Dispatchers.IO) {
                val host = "${storageAccount}.blob.core.windows.net"
                val address = java.net.InetAddress.getByName(host)
                println("✅ DNS resolvido: ${address.hostAddress}")
            }
            true
        } catch (e: Exception) {
            println("❌ Falha no teste de conectividade: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Verifica se uma URL de blob é acessível (HEAD request).
     * Retorna true se status for 2xx.
     */
    suspend fun isUrlAccessible(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val code = connection.responseCode
            connection.disconnect()
            println("🔎 HEAD $url -> $code")
            return@withContext code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            println("🔎 HEAD failed for $url: ${e.message}")
            return@withContext false
        }
    }
}

/**
 * Upload genérico de arquivo para Azure Blob Storage
 * @deprecated Use AzureBlobRetrofit.uploadImageToAzure instead
 */
suspend fun uploadFileToAzure(
    file: File,
    storageAccount: String,
    accountKey: String,
    containerName: String
): String? {
    // Redireciona para a função principal
    return AzureBlobRetrofit.uploadImageToAzure(file, storageAccount, accountKey, containerName)
}