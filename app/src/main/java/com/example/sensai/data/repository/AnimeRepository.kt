package com.example.sensai.data.repository

import com.example.sensai.data.network.BackendApi
import com.example.sensai.data.network.dto.AnimeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface AnimeRepository {
    fun searchAnime(query: String): Flow<Result<List<AnimeDto>>>
    fun getTopAnime(): Flow<Result<List<AnimeDto>>>
    fun getSeasonalAnime(): Flow<Result<List<AnimeDto>>>
    fun getAnimeDetails(id: Int): Flow<Result<AnimeDto>>
    fun toggleFavorite(animeId: Int, isAdding: Boolean): Flow<Result<Unit>>
    fun addToHistory(animeId: Int, status: String?): Flow<Result<Unit>>
    fun getRecommendations(): Flow<Result<com.example.sensai.data.network.dto.JikanResponse>>
    fun getUserHistory(): Flow<Result<List<com.example.sensai.data.network.dto.AnimeHistoryDto>>>
    fun getUserFavorites(): Flow<Result<List<com.example.sensai.data.network.dto.FavoriteDto>>>
    fun getDailyQuiz(): Flow<Result<List<com.example.sensai.data.network.dto.quiz.QuizQuestionDto>>>
    fun submitQuiz(score: Int, totalQuestions: Int): Flow<Result<com.example.sensai.data.network.dto.quiz.QuizSessionDto>>
    fun getLeaderboard(): Flow<Result<List<com.example.sensai.data.network.dto.UserDto>>>
}

class AnimeRepositoryImpl @Inject constructor(
    private val backendApi: BackendApi
) : AnimeRepository {

    override fun searchAnime(query: String): Flow<Result<List<AnimeDto>>> = flow {
        try {
            val response = backendApi.searchAnime(query)
            emit(Result.success(response.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTopAnime(): Flow<Result<List<AnimeDto>>> = flow {
        try {
            val response = backendApi.getTopAnime()
            emit(Result.success(response.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSeasonalAnime(): Flow<Result<List<AnimeDto>>> = flow {
        try {
            val response = backendApi.getSeasonalAnime()
            emit(Result.success(response.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Result<AnimeDto>> = flow {
        try {
            val response = backendApi.getAnimeById(id)
            emit(Result.success(response.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun toggleFavorite(animeId: Int, isAdding: Boolean): Flow<Result<Unit>> = flow {
        try {
            if (isAdding) {
                backendApi.addToFavorites(animeId)
            } else {
                backendApi.removeFromFavorites(animeId)
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun addToHistory(animeId: Int, status: String?): Flow<Result<Unit>> = flow {
        try {
            if (status != null) {
                backendApi.addToHistory(animeId, status)
            } else {
                backendApi.removeFromHistory(animeId)
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getRecommendations(): Flow<Result<com.example.sensai.data.network.dto.JikanResponse>> = flow {
        try {
            val response = backendApi.getSenseiRecommendations()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getUserHistory(): Flow<Result<List<com.example.sensai.data.network.dto.AnimeHistoryDto>>> = flow {
        try {
            val response = backendApi.getUserHistory()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getUserFavorites(): Flow<Result<List<com.example.sensai.data.network.dto.FavoriteDto>>> = flow {
        try {
            val response = backendApi.getUserFavorites()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getDailyQuiz(): Flow<Result<List<com.example.sensai.data.network.dto.quiz.QuizQuestionDto>>> = flow {
        try {
            val response = backendApi.getDailyQuiz()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun submitQuiz(score: Int, totalQuestions: Int): Flow<Result<com.example.sensai.data.network.dto.quiz.QuizSessionDto>> = flow {
        try {
            val request = com.example.sensai.data.network.dto.quiz.QuizSubmitDto(score, totalQuestions)
            val response = backendApi.submitQuiz(request)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLeaderboard(): Flow<Result<List<com.example.sensai.data.network.dto.UserDto>>> = flow {
        try {
            val response = backendApi.getLeaderboard()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
