package com.application.metriq.network

import com.application.metriq.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApi {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = BuildConfig.USDA_API_KEY
    ): FoodSearchResponse
}
