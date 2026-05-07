package com.example.sensai.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.AnimeDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnimeDetailUiState(
    val anime: AnimeDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val watchStatus: String? = null // "watching", "completed"
)

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    init {
        loadAnimeDetails()
    }

    private fun loadAnimeDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Fetch basic details
            repository.getAnimeDetails(animeId).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { it.copy(anime = data) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            }

            // Sync with history
            repository.getUserHistory().collect { result ->
                result.onSuccess { historyList ->
                    val historyItem = historyList.find { it.animeId == animeId }
                    _uiState.update { it.copy(watchStatus = historyItem?.status) }
                }
            }

            // Sync with favorites
            repository.getUserFavorites().collect { result ->
                result.onSuccess { favoriteList ->
                    val isFav = favoriteList.any { it.animeId == animeId }
                    _uiState.update { it.copy(isFavorite = isFav) }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleFavorite() {
        val newState = !_uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = newState) }
        viewModelScope.launch {
            repository.toggleFavorite(animeId, newState).collect { result ->
                result.onFailure { e ->
                    // Revert on failure
                    _uiState.update { it.copy(isFavorite = !newState, error = "Failed to update favorite: ${e.message}") }
                }
            }
        }
    }

    fun setWatchStatus(status: String) {
        val oldStatus = _uiState.value.watchStatus
        val newStatus = if (oldStatus == status) null else status
        _uiState.update { it.copy(watchStatus = newStatus) }
        
        if (newStatus != null) {
            viewModelScope.launch {
                repository.addToHistory(animeId, newStatus).collect { result ->
                    result.onFailure { e ->
                        // Revert on failure
                        _uiState.update { it.copy(watchStatus = oldStatus, error = "Failed to update history: ${e.message}") }
                    }
                }
            }
        }
    }
}
