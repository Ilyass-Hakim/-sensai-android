package com.example.sensai.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.AnimeDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val topAnime: List<AnimeDto> = emptyList(),
    val seasonalAnime: List<AnimeDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeData()
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            var topList: List<AnimeDto> = emptyList()
            var seasonalList: List<AnimeDto> = emptyList()
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

            _uiState.value = _uiState.value.copy(
                topAnime = topList,
                seasonalAnime = seasonalList,
                isLoading = false,
                error = errorMsg
            )
        }
    }
}
