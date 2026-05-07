package com.example.sensai.data.network.dto.chat

import com.google.gson.annotations.SerializedName

data class ChatSendMessageRequest(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("content") val content: String
)

data class ChatJoinRequest(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("userId") val userId: Long
)
