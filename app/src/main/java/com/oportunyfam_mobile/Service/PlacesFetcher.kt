package com.oportunyfam_mobile.Service

import android.content.Context
import android.content.pm.PackageManager
import com.oportunyfam_mobile.model.OngMapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

suspend fun fetchPlacesFromGoogle(
    context: Context,
    lat: Double,
    lon: Double,
    typeFilter: String? = null
): List<OngMapMarker> = withContext(Dispatchers.IO) {
    try {
        val appInfo = try {
            context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            null
        }
        val apiKey = appInfo?.metaData?.getString("com.google.android.geo.API_KEY")
        if (apiKey.isNullOrBlank()) return@withContext emptyList()

        // build a small Retrofit instance locally to avoid cross-file resolution issues
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PlacesService::class.java)

        val locationParam = "${lat},${lon}"
        val resultsAccum = mutableListOf<OngMapMarker>()

        // Se o filtro for especificamente "ngo", buscar por palavras-chave
        if (typeFilter == "ngo") {
            val ongKeywords = listOf("ONG", "NGO", "instituto social", "assistência social", "projeto social", "instituição beneficente")
            for (keyword in ongKeywords) {
                try {
                    val response = service.nearbySearch(locationParam, 5000, null, keyword, apiKey)
                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.results?.forEach { r: PlaceResult ->
                            val latR = r.geometry.location.lat
                            val lngR = r.geometry.location.lng
                            if (resultsAccum.none { it.placeId == r.place_id }) {
                                resultsAccum.add(
                                    OngMapMarker(
                                        id = r.place_id.hashCode(),
                                        nome = r.name,
                                        latitude = latR,
                                        longitude = lngR,
                                        categorias = emptyList(),
                                        descricao = r.vicinity ?: "",
                                        endereco = r.vicinity ?: "",
                                        telefone = "",
                                        email = "",
                                        isExternal = true,
                                        placeId = r.place_id,
                                        types = listOf("ngo")
                                    )
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // ignore keyword errors and continue
                }
            }
            return@withContext resultsAccum.distinctBy { it.placeId }
        }

        // Se um filtro foi especificado, usar apenas ele; caso contrário, buscar todos os tipos relevantes
        val typesToQuery = if (typeFilter != null) {
            listOf(typeFilter)
        } else {
            listOf(
                "school",                    // Escolas
                "library",                   // Bibliotecas
                "gym",                       // Academias
                "community_center",          // Centros comunitários
                "park",                      // Parques
                "stadium",                   // Centros esportivos/estádios
                "child_care_agency",         // Creches e cuidados infantis
                "church",                    // Igrejas/templos (muitos têm programas sociais)
                "local_government_office",   // Órgãos públicos com programas sociais
                "point_of_interest"          // Pontos de interesse gerais
            )
        }

        for (t in typesToQuery) {
            try {
                val response = service.nearbySearch(locationParam, 5000, t, null, apiKey)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.results?.forEach { r: PlaceResult ->
                        val latR = r.geometry.location.lat
                        val lngR = r.geometry.location.lng
                        resultsAccum.add(
                            OngMapMarker(
                                id = r.place_id.hashCode(),
                                nome = r.name,
                                latitude = latR,
                                longitude = lngR,
                                categorias = emptyList(),
                                descricao = r.vicinity ?: "",
                                endereco = r.vicinity ?: "",
                                telefone = "",
                                email = "",
                                isExternal = true,
                                placeId = r.place_id,
                                types = listOf(t) // Armazenar o tipo para filtro
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // ignore per-type errors and continue
            }
        }

        // Buscar especificamente por ONGs e instituições sociais usando keywords
        // (Google Places não tem tipo específico para NGO/ONG)
        if (typeFilter == null || typeFilter == "point_of_interest") {
            val ongKeywords = listOf("ONG", "NGO", "instituto social", "assistência social", "projeto social")
            for (keyword in ongKeywords) {
                try {
                    val response = service.nearbySearch(locationParam, 5000, null, keyword, apiKey)
                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.results?.forEach { r: PlaceResult ->
                            val latR = r.geometry.location.lat
                            val lngR = r.geometry.location.lng
                            // Verificar se já existe para evitar duplicatas
                            if (resultsAccum.none { it.placeId == r.place_id }) {
                                resultsAccum.add(
                                    OngMapMarker(
                                        id = r.place_id.hashCode(),
                                        nome = r.name,
                                        latitude = latR,
                                        longitude = lngR,
                                        categorias = emptyList(),
                                        descricao = r.vicinity ?: "",
                                        endereco = r.vicinity ?: "",
                                        telefone = "",
                                        email = "",
                                        isExternal = true,
                                        placeId = r.place_id,
                                        types = listOf("ngo") // Tipo customizado para ONGs
                                    )
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // ignore keyword errors and continue
                }
            }
        }

        resultsAccum.distinctBy { it.placeId }
    } catch (_: Exception) {
        emptyList()
    }
}

suspend fun fetchPlaceDetails(context: Context, placeId: String): PlaceDetailsResult? = withContext(Dispatchers.IO) {
    try {
        val appInfo = try { context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA) } catch (e: Exception) { null }
        val apiKey = appInfo?.metaData?.getString("com.google.android.geo.API_KEY")
        if (apiKey.isNullOrBlank()) return@withContext null

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PlacesService::class.java)
        // Pedimos campos mínimos: name, formatted_address, formatted_phone_number, website, rating
        val fields = "place_id,name,formatted_address,formatted_phone_number,website,rating"
        val resp = service.placeDetails(placeId, fields, apiKey)
        if (resp.isSuccessful) {
            resp.body()?.result
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


