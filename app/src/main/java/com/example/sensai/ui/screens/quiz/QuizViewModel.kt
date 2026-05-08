package com.example.sensai.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.dto.quiz.QuizQuestionDto
import com.example.sensai.data.network.dto.quiz.QuizSessionDto
import com.example.sensai.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<QuizQuestionDto> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val timeLeft: Int = 15,
    val selectedAnswer: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val sessionResult: QuizSessionDto? = null,
    val error: String? = null,
    val showXpAnimation: Boolean = false
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadDailyQuiz()
    }

    private fun loadDailyQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getDailyQuiz().collect { result ->
                result.onSuccess { questions ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questions = questions,
                            currentQuestionIndex = 0,
                            score = 0,
                            isFinished = questions.isEmpty()
                        )
                    }
                    if (questions.isNotEmpty()) {
                        startTimer()
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load quiz: ${e.message}") }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timeLeft = 15) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            // Time's up
            submitAnswer("")
        }
    }

    fun submitAnswer(answer: String) {
        if (_uiState.value.selectedAnswer != null) return // Already answered
        
        timerJob?.cancel()
        
        val currentQuestion = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = answer == currentQuestion.correctAnswer
        
        _uiState.update {
            it.copy(
                selectedAnswer = answer,
                isAnswerCorrect = isCorrect,
                score = if (isCorrect) it.score + 10 else it.score,
                showXpAnimation = isCorrect
            )
        }

        // Wait a bit to show feedback, then move to next question
        viewModelScope.launch {
            delay(1500)
            _uiState.update { it.copy(showXpAnimation = false) }
            
            if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
                _uiState.update {
                    it.copy(
                        currentQuestionIndex = it.currentQuestionIndex + 1,
                        selectedAnswer = null,
                        isAnswerCorrect = null
                    )
                }
                startTimer()
            } else {
                finishQuiz()
            }
        }
    }

    private fun finishQuiz() {
        _uiState.update { it.copy(isFinished = true, isLoading = true) }
        viewModelScope.launch {
            repository.submitQuiz(_uiState.value.score, _uiState.value.questions.size).collect { result ->
                result.onSuccess { session ->
                    _uiState.update { it.copy(isLoading = false, sessionResult = session) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to submit quiz: ${e.message}") }
                }
            }
        }
    }
}
