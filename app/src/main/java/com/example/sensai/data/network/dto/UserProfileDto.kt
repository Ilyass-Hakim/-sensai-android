package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("xp") val xp: Int,
    @SerializedName("rank") val rank: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("preferredGenres") val preferredGenres: List<String>?,
    @SerializedName("animeCompleted") val animeCompleted: Int,
    @SerializedName("favoritesCount") val favoritesCount: Int,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
