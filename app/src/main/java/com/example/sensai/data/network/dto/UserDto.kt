package com.example.sensai.data.network.dto

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val xp: Int,
    val rank: String,
    val preferredGenres: List<String>? = null
)
