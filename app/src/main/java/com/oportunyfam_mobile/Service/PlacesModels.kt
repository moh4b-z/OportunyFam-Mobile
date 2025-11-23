package com.oportunyfam_mobile.Service

// Modelos mínimos para resposta da Places Nearby Search
data class PlacesResponse(
    val results: List<PlaceResult> = emptyList(),
    val status: String? = null,
    val next_page_token: String? = null
)

data class PlaceResult(
    val place_id: String,
    val name: String,
    val geometry: Geometry,
    val vicinity: String? = null,
    val types: List<String>? = null
)

data class Geometry(
    val location: LatLngJson
)

data class LatLngJson(
    val lat: Double,
    val lng: Double
)

