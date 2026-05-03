package com.example.sensai.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.AnimeDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val searchResults: List<AnimeDto> = emptyList(),
    val isLoading: Boolean = false,
    val selectedMoodIndex: Int = -1,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchQueryFlow.value = newQuery
        
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
        }
    }

    fun onMoodSelected(index: Int) {
        _uiState.update { it.copy(selectedMoodIndex = if (it.selectedMoodIndex == index) -1 else index) }
        // For a real app, this could map to specific genre IDs in Jikan API
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.searchAnime(query).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { it.copy(searchResults = data, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }
}
