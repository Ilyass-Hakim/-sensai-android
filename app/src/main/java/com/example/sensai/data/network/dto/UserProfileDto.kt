package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String? = null,
    @SerializedName("username") val username: String,
    @SerializedName("xp") val xp: Int,
    @SerializedName("level") val level: Int = 1,
    @SerializedName("rank") val rank: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("preferredGenres") val preferredGenres: List<String>? = null,
    @SerializedName("animeCompleted") val animeCompleted: Int = 0,
    @SerializedName("favoritesCount") val favoritesCount: Int = 0,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)
