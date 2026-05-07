package com.example.sensai.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("rank")
    val rank: String? = null
)
