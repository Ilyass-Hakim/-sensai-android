package com.example.sensai.ui.screens.quizhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.quiz.QuizSessionDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizHistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<QuizSessionDto> = emptyList(),
    val error: String? = null
) {
    val totalXp: Int get() = sessions.sumOf { it.score * 2 }
    val bestScore: Int get() = sessions.maxOfOrNull { it.score } ?: 0
    val totalGames: Int get() = sessions.size
    val averageScore: Double
        get() = if (sessions.isEmpty()) 0.0
                else sessions.sumOf { it.score }.toDouble() / sessions.size
    val averageAccuracy: Double
        get() = if (sessions.isEmpty()) 0.0
                else sessions.sumOf { s ->
                    if (s.totalQuestions > 0) s.score.toDouble() / s.totalQuestions else 0.0
                } / sessions.size * 100
}

@HiltViewModel
class QuizHistoryViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizHistoryUiState())
    val uiState: StateFlow<QuizHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getQuizHistory().collect { result ->
                result.onSuccess { sessions ->
                    _uiState.update { it.copy(isLoading = false, sessions = sessions) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load quiz history") }
                }
            }
        }
    }
}
