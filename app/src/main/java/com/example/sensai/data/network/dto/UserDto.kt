package com.example.sensai.data.network.dto

data class UserDto(
    val id: Long,
    val username: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val xp: Int,
    val level: Int,
    val rank: String,
    val preferredGenres: List<String>? = null
)
