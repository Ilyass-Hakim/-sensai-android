package com.example.sensai.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.AnimeHistoryDto
import com.example.sensai.data.network.dto.FavoriteDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WatchlistTab { HISTORY, FAVORITES }

data class WatchlistUiState(
    val isLoading: Boolean = true,
    val history: List<AnimeHistoryDto> = emptyList(),
    val favorites: List<FavoriteDto> = emptyList(),
    val activeTab: WatchlistTab = WatchlistTab.HISTORY,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            var historyList: List<AnimeHistoryDto> = emptyList()
            var favoritesList: List<FavoriteDto> = emptyList()
            var errorMsg: String? = null

            repository.getUserHistory().collect { result ->
                result.onSuccess { historyList = it }
                      .onFailure { errorMsg = "Failed to load history: ${it.message}" }
            }

            repository.getUserFavorites().collect { result ->
                result.onSuccess { favoritesList = it }
                      .onFailure { if (errorMsg == null) errorMsg = "Failed to load favorites: ${it.message}" }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    history = historyList,
                    favorites = favoritesList,
                    error = errorMsg
                )
            }
        }
    }

    fun setTab(tab: WatchlistTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun removeFromHistory(animeId: Int) {
        viewModelScope.launch {
            repository.addToHistory(animeId, null).collect { result ->
                result.onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            history = state.history.filter { it.animeId != animeId },
                            successMessage = "Removed from history"
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = "Failed to remove: ${e.message}") }
                }
            }
        }
    }

    fun removeFromFavorites(animeId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(animeId, false).collect { result ->
                result.onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            favorites = state.favorites.filter { it.animeId != animeId },
                            successMessage = "Removed from favorites"
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = "Failed to remove: ${e.message}") }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
