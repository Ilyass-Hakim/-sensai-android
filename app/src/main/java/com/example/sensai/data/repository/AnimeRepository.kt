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
}
