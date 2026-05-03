package com.example.sensai.data.network

import com.example.sensai.data.network.dto.JikanResponse
import com.example.sensai.data.network.dto.JikanSingleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendApi {
    @GET("search")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): JikanResponse

    @GET("top")
    suspend fun getTopAnime(): JikanResponse

    @GET("seasonal")
    suspend fun getSeasonalAnime(): JikanResponse

    @GET("{id}")
    suspend fun getAnimeById(
        @Path("id") id: Int
    ): JikanSingleResponse
}
