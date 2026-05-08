package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class UpdateProfileDto(
    @SerializedName("username") val username: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("preferredGenres") val preferredGenres: List<String>?
)
