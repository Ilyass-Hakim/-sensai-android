package com.example.sensai.data.repository

import com.example.sensai.data.network.BackendApi
import com.example.sensai.data.network.dto.ChatRequest
import com.example.sensai.data.network.dto.ChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface AiRepository {
    fun chatWithAi(message: String): Flow<Result<ChatResponse>>
    fun clearChatHistory(): Flow<Result<Unit>>
}

class AiRepositoryImpl @Inject constructor(
    private val backendApi: BackendApi
) : AiRepository {

    override fun chatWithAi(message: String): Flow<Result<ChatResponse>> = flow {
        try {
            val response = backendApi.chatWithAi(ChatRequest(message))
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun clearChatHistory(): Flow<Result<Unit>> = flow {
        try {
            backendApi.clearAiChatHistory()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
