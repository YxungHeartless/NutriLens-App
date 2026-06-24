package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApiService {
    @GET("fdc/v1/foods/search")
    suspend fun searchFoods(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 5,
        @Query("dataType") dataType: String? = "Foundation,SR Legacy"
    ): UsdaSearchResponse
}

data class UsdaSearchResponse(
    val foods: List<UsdaFoodItem>?
)

data class UsdaFoodItem(
    val fdcId: Int,
    val description: String,
    val dataType: String?,
    val foodNutrients: List<UsdaFoodNutrient>?
)

data class UsdaFoodNutrient(
    val nutrientId: Int?,
    val nutrientName: String?,
    val value: Double?,
    val unitName: String?
)
