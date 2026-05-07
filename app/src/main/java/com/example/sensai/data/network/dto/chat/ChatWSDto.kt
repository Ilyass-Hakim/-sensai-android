package com.example.sensai.data.network.dto.chat

data class ChatWSMessage(
    val id: Long,
    val roomId: Long,
    val userId: Long,
    val username: String,
    val content: String,
    val createdAt: String
)

data class ChatWSReaction(
    val messageId: Long,
    val roomId: Long,
    val userId: Long,
    val username: String,
    val emoji: String
)

data class ChatRoom(
    val id: Long,
    val name: String,
    val description: String?,
    val type: String,
    val animeId: Int?,
    val createdAt: String
)
