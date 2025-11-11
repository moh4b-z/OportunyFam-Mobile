package com.oportunyfam_mobile

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import coil.request.CachePolicy
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Application class customizada para configurar o Coil ImageLoader
 * com suporte otimizado para carregar imagens do Azure Storage via HTTPS
 */
class OportunyFamApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Inicialização da aplicação
    }

    override fun newImageLoader(): ImageLoader {
        // Configura OkHttpClient com timeouts maiores para imagens remotas
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .respectCacheHeaders(false) // Ignora headers de cache do servidor
            .diskCachePolicy(CachePolicy.DISABLED) // Desabilita cache em disco por padrão
            .memoryCachePolicy(CachePolicy.ENABLED) // Mantém cache em memória para performance
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Usa 25% da memória disponível
                    .build()
            }
            .build()
    }
}

