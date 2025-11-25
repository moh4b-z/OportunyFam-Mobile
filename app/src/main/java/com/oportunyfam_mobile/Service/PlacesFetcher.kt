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
        // Se um filtro foi especificado, usar apenas ele; caso contrário, buscar todos os tipos
        val typesToQuery = if (typeFilter != null) {
            listOf(typeFilter)
        } else {
            listOf("school", "library", "gym", "point_of_interest")
        }
        val resultsAccum = mutableListOf<OngMapMarker>()

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


