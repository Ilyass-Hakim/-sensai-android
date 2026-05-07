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

    @GET("anime/sensei-recommendations")
    suspend fun getSenseiRecommendations(): JikanResponse

    @GET("anime/{id}")
    suspend fun getAnimeById(
        @Path("id") id: Int
    ): JikanSingleResponse

    @retrofit2.http.POST("anime/favorites")
    suspend fun addToFavorites(
        @Query("animeId") animeId: Int
    ): Any

    @retrofit2.http.DELETE("anime/favorites")
    suspend fun removeFromFavorites(
        @Query("animeId") animeId: Int
    ): Any

    @retrofit2.http.POST("anime/history")
    suspend fun addToHistory(
        @Query("animeId") animeId: Int,
        @Query("status") status: String
    ): Any

    @GET("anime/history")
    suspend fun getUserHistory(): List<com.example.sensai.data.network.dto.AnimeHistoryDto>

    @GET("anime/favorites")
    suspend fun getUserFavorites(): List<com.example.sensai.data.network.dto.FavoriteDto>
    @retrofit2.http.POST("ai/chat")
    suspend fun chatWithAi(
        @retrofit2.http.Body request: com.example.sensai.data.network.dto.ChatRequest
    ): com.example.sensai.data.network.dto.ChatResponse

    @retrofit2.http.DELETE("ai/chat/history")
    suspend fun clearAiChatHistory(): Any

    @GET("chat/rooms")
    suspend fun getChatRooms(): List<com.example.sensai.data.network.dto.chat.ChatRoom>

    @GET("chat/rooms/{roomId}/history")
    suspend fun getRoomHistory(@Path("roomId") roomId: Long): List<com.example.sensai.data.network.dto.chat.ChatWSMessage>
    @retrofit2.http.POST("chat/rooms/anime/{animeId}")
    suspend fun getOrCreateAnimeRooms(
        @Path("animeId") animeId: Int,
        @Query("animeName") animeName: String
    ): List<com.example.sensai.data.network.dto.chat.ChatRoom>
}
