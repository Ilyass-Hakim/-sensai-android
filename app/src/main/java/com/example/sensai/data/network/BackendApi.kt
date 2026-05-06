package com.example.sensai.data.network

import com.example.sensai.data.network.dto.JikanResponse
import com.example.sensai.data.network.dto.JikanSingleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendApi {
    @GET("anime/search")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): JikanResponse

    @GET("anime/top")
    suspend fun getTopAnime(): JikanResponse

    @GET("anime/seasonal")
    suspend fun getSeasonalAnime(): JikanResponse

    @GET("anime/{id}")
    suspend fun getAnimeById(
        @Path("id") id: Int
    ): JikanSingleResponse
}
