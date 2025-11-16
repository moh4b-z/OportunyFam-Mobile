package com.oportunyfam_mobile.Service

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import kotlinx.coroutines.tasks.await

/**
 * Modelo para representar uma instituição não cadastrada (do Google Places)
 */
data class PlaceInstituicao(
    val id: String,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val endereco: String?,
    val tipos: List<String>
)

/**
 * Serviço para buscar instituições não cadastradas usando Google Places API
 */
class PlacesService(private val context: Context) {

    private var placesClient: PlacesClient? = null

    init {
        try {
            // Inicializar Places API com a chave do AndroidManifest.xml
            if (!Places.isInitialized()) {
                Places.initialize(context.applicationContext, getApiKey())
            }
            placesClient = Places.createClient(context)
        } catch (e: Exception) {
            Log.e("PlacesService", "Erro ao inicializar Places API", e)
        }
    }

    /**
     * Obter a chave de API do AndroidManifest.xml
     */
    private fun getApiKey(): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            Log.e("PlacesService", "Erro ao obter API Key", e)
            ""
        }
    }

    /**
     * Buscar instituições de caridade, ONGs e centros sociais nas proximidades
     * Limitado ao Brasil
     */
    suspend fun buscarInstituicoesProximas(
        localizacao: LatLng,
        raioKm: Double = 10.0
    ): List<PlaceInstituicao> {
        val client = placesClient ?: run {
            Log.e("PlacesService", "PlacesClient não inicializado")
            return emptyList()
        }

        val instituicoes = mutableListOf<PlaceInstituicao>()

        // Tipos de lugares para buscar (relacionados a instituições de caridade e ONGs)
        val tiposDeBusca = listOf(
            "ong",
            "instituição de caridade",
            "centro social",
            "centro esportivo",
            "ajuda social",
            "assistência social",
            "filantropia",
            "voluntariado",
            "doação"
        )

        try {
            // Criar bounds para limitar busca ao Brasil
            val brasilBounds = RectangularBounds.newInstance(
                LatLng(-33.7505, -73.9872), // Sudoeste do Brasil
                LatLng(5.2719, -34.7299)    // Nordeste do Brasil
            )

            for (tipo in tiposDeBusca) {
                try {
                    // Criar requisição de busca por texto
                    val request = SearchByTextRequest.builder(tipo, getPlaceFields())
                        .setLocationBias(brasilBounds)
                        .setMaxResultCount(5) // Limitar resultados por tipo
                        .build()

                    // Executar busca
                    val response = client.searchByText(request).await()

                    // Processar resultados
                    response.places.forEach { place ->
                        val location = place.latLng
                        if (location != null) {
                            // Verificar se está dentro do raio especificado
                            val distancia = calcularDistancia(
                                localizacao.latitude, localizacao.longitude,
                                location.latitude, location.longitude
                            )

                            if (distancia <= raioKm) {
                                instituicoes.add(
                                    PlaceInstituicao(
                                        id = place.id ?: "unknown",
                                        nome = place.name ?: "Sem nome",
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        endereco = place.address,
                                        tipos = emptyList() // Tipos estão deprecated na nova API
                                    )
                                )
                            }
                        }
                    }

                    Log.d("PlacesService", "Tipo '$tipo': ${response.places.size} lugares encontrados")
                } catch (e: Exception) {
                    Log.e("PlacesService", "Erro ao buscar tipo '$tipo'", e)
                }
            }

            // Remover duplicatas baseado no ID
            val instituicoesUnicas = instituicoes.distinctBy { it.id }
            Log.d("PlacesService", "Total de instituições únicas encontradas: ${instituicoesUnicas.size}")

            return instituicoesUnicas

        } catch (e: Exception) {
            Log.e("PlacesService", "Erro ao buscar instituições", e)
            return emptyList()
        }
    }

    /**
     * Campos do Place que queremos buscar
     */
    private fun getPlaceFields(): List<Place.Field> {
        return listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.TYPES
        )
    }

    /**
     * Calcular distância entre dois pontos em km (Fórmula de Haversine)
     */
    private fun calcularDistancia(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Raio da Terra em km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

