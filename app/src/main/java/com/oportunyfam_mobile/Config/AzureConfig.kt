package com.oportunyfam_mobile.Config

import android.util.Log

/**
 * Configurações do Azure Storage
 * IMPORTANTE: Nunca commite chaves de acesso no Git!
 * Use variáveis de ambiente ou arquivo local.properties
 */
object AzureConfig {
    private const val AZURE_STORAGE_KEY = "sp=racwl&st=2025-11-18T13:06:56Z&se=2025-12-05T21:21:56Z&sv=2024-11-04&sr=c&sig=blfBJt5Lw0S9tB1mSpo%2FRufvFq5eXaPQNFI3mZ36Z5Y%3D"

    /**
     * Obtém a chave de acesso do Azure Storage
     * Prioridade:
     * 1. Chave configurada no código (AZURE_STORAGE_KEY)
     * 2. BuildConfig (carregado do local.properties)
     * 3. Variável de ambiente AZURE_STORAGE_KEY
     * 4. Propriedade do sistema azure.storage.key
     *
     * @return A chave de acesso ou null se não configurada
     */
    fun getStorageKey(): String? {
        // Retorna a chave configurada diretamente
        if (AZURE_STORAGE_KEY.isNotBlank()) {
            Log.d("AzureConfig", "✅ Usando chave configurada diretamente no código")
            return AZURE_STORAGE_KEY
        }

        // Tenta obter do BuildConfig (só funciona após Gradle sync)
        try {
            val buildConfigClass = Class.forName("com.oportunyfam_mobile_ong.BuildConfig")
            val field = buildConfigClass.getField("AZURE_STORAGE_KEY")
            val key = field.get(null) as? String
            if (!key.isNullOrBlank()) {
                Log.d("AzureConfig", "✅ Usando chave do BuildConfig")
                return key
            }
        } catch (e: Exception) {
            // BuildConfig ainda não foi gerado - Gradle sync necessário
            Log.d("AzureConfig", "BuildConfig não disponível, tentando outras fontes")
        }

        // Tenta obter da variável de ambiente
        val envKey = System.getenv("AZURE_STORAGE_KEY")
        if (!envKey.isNullOrBlank()) {
            Log.d("AzureConfig", "✅ Usando chave da variável de ambiente")
            return envKey
        }

        // Tenta obter da propriedade do sistema
        val sysKey = System.getProperty("azure.storage.key")
        if (!sysKey.isNullOrBlank()) {
            Log.d("AzureConfig", "✅ Usando chave da propriedade do sistema")
            return sysKey
        }

        // Log de aviso se não configurado
        Log.w("AzureConfig", """
            ⚠️ Azure Storage Key não configurada!
            
            Para habilitar upload de imagens:
            1. Configure a chave no arquivo local.properties:
               azure.storage.key=SUA_CHAVE_AQUI
            2. Faça Sync do Gradle: File > Sync Project with Gradle Files
            
            Ou configure a variável de ambiente AZURE_STORAGE_KEY
            
            IMPORTANTE: Nunca commite chaves de acesso no Git!
        """.trimIndent())

        return null
    }

    /**
     * Verifica se o Azure Storage está configurado
     */
    fun isConfigured(): Boolean {
        return getStorageKey() != null
    }

    const val STORAGE_ACCOUNT = "oportunyfamstorage"
    const val CONTAINER_PERFIL = "imagens-perfil"
    const val CONTAINER_PUBLICACOES = "imagens-publicacoes"
}