package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class AnimeHistoryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("animeId") val animeId: Int,
    @SerializedName("animeTitle") val animeTitle: String?,
    @SerializedName("animeImageUrl") val animeImageUrl: String?,
    @SerializedName("status") val status: String,
    @SerializedName("episodesWatched") val episodesWatched: Int,
    @SerializedName("userRating") val userRating: Double?
)

data class FavoriteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("animeId") val animeId: Int,
    @SerializedName("animeTitle") val animeTitle: String?,
    @SerializedName("animeImageUrl") val animeImageUrl: String?
)
