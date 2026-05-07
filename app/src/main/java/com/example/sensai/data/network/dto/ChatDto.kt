package com.example.sensai.data.network.dto

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val text: String,
    val emotion: String,
    val audioBase64: String? = null
)
