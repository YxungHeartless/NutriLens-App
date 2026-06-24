package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearbyPlaces(
        @Query("location") location: String, // "lat,lng"
        @Query("radius") radius: Double,
        @Query("type") type: String = "restaurant",
        @Query("keyword") keyword: String? = null,
        @Query("key") apiKey: String
    ): PlacesSearchResponse
}

data class PlacesSearchResponse(
    val results: List<PlaceResult>?,
    val status: String?
)

data class PlaceResult(
    val place_id: String?,
    val name: String?,
    val rating: Double?,
    val types: List<String>?,
    val geometry: PlaceGeometry?
)

data class PlaceGeometry(
    val location: PlaceLocation?
)

data class PlaceLocation(
    val lat: Double?,
    val lng: Double?
)
