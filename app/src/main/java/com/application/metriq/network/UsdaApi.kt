package com.application.metriq.network

import com.application.metriq.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UsdaApi {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("dataType") dataTypes: List<String>,
        @Query("api_key") apiKey: String = BuildConfig.USDA_API_KEY
    ): FoodSearchResponse

    @GET("foods/{fdcId}")
    suspend fun getFoodDetails(
        @Path("fdcId") fdcId: Int,
        @Query("api_key") apiKey: String = BuildConfig.USDA_API_KEY
    ): Food
}
