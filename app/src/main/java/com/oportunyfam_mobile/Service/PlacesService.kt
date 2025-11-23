package com.oportunyfam_mobile.Service

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun nearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String?,
        @Query("keyword") keyword: String?,
        @Query("key") apiKey: String
    ): Response<PlacesResponse>

    @GET("maps/api/place/details/json")
    suspend fun placeDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String,
        @Query("key") apiKey: String
    ): Response<PlaceDetailsResponse>
}
