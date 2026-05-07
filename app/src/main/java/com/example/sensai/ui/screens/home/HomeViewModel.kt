package com.example.sensai.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.AnimeDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val topAnime: List<AnimeDto> = emptyList(),
    val seasonalAnime: List<AnimeDto> = emptyList(),
    val recommendations: List<AnimeDto> = emptyList(),
    val senseiMessage: String = "Bonjour ! Prêt à découvrir de nouveaux anime ?",
    val isLoading: Boolean = true,
    val error: String? = null,
    val username: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val tokenManager: com.example.sensai.data.local.TokenManager
) : ViewModel() {

    val token = tokenManager.accessToken

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            var topList: List<AnimeDto> = emptyList()
            var seasonalList: List<AnimeDto> = emptyList()
            var recoList: List<AnimeDto> = emptyList()
            var errorMsg: String? = null

            // Fetch Top Anime
            repository.getTopAnime().collect { result ->
                result.onSuccess { topList = it }
                      .onFailure { errorMsg = it.message }
            }

            // Fetch Seasonal
            repository.getSeasonalAnime().collect { result ->
                result.onSuccess { seasonalList = it }
                      .onFailure { errorMsg = it.message }
            }

            repository.getRecommendations().collect { result ->
                result.onSuccess { response ->
                    recoList = response.data
                }.onFailure {
                    errorMsg = "Recs error: ${it.message}"
                }
            }

            val finalRecos = if (recoList.isEmpty()) topList.take(10) else recoList
            val senseiMsg = if (recoList.isEmpty()) {
                "Commence à regarder des anime pour des recs personnalisées !"
            } else {
                "J'ai sélectionné quelque chose spécialement pour toi aujourd'hui !"
            }
            
            // Fetch Username once
            val name = tokenManager.username.firstOrNull()
            
            _uiState.value = _uiState.value.copy(
                topAnime = topList,
                seasonalAnime = seasonalList,
                recommendations = finalRecos,
                senseiMessage = senseiMsg,
                isLoading = false,
                error = errorMsg,
                username = name ?: "User"
            )
        }
    }
}
