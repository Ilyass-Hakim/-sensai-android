package com.example.sensai.ui.sensei

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.sensai.data.repository.AiRepository
import com.example.sensai.data.local.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


data class Message(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val audioBase64: String? = null,
    val videoBase64: String? = null
)

data class SenseiUiState(
    val messages: List<Message> = emptyList(),
    val isTyping: Boolean = false,
    val inputText: String = "",
    val isVoiceEnabled: Boolean = true,
    val currentVideoBase64: String? = null
)



@HiltViewModel
class SenseiViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SenseiUiState())
    val uiState: StateFlow<SenseiUiState> = _uiState.asStateFlow()

    private val _audioEvents = MutableSharedFlow<String>()
    val audioEvents = _audioEvents.asSharedFlow()

    private val _videoEvents = MutableSharedFlow<String>()
    val videoEvents = _videoEvents.asSharedFlow()

    init {
        // Observe voice preference
        viewModelScope.launch {
            tokenManager.isVoiceEnabled.collect { enabled ->
                _uiState.update { it.copy(isVoiceEnabled = enabled) }
            }
        }

        // Initial welcome message
        _uiState.update { 
            it.copy(messages = listOf(Message("1", "Bonjour ! Je suis ton Sensei. Comment puis-je t'aider aujourd'hui ?", false)))
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun toggleVoice() {
        viewModelScope.launch {
            tokenManager.setVoiceEnabled(!_uiState.value.isVoiceEnabled)
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val userMessage = Message(
            id = System.currentTimeMillis().toString(),
            text = text,
            isFromUser = true
        )

        _uiState.update { 
            it.copy(
                messages = listOf(userMessage) + it.messages,
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            aiRepository.chatWithAi(text).collect { result ->
                result.onSuccess { response ->
                    val senseiResponse = Message(
                        id = System.currentTimeMillis().toString(),
                        text = response.text,
                        isFromUser = false,
                        audioBase64 = response.audioBase64
                    )
                    _uiState.update { 
                        it.copy(
                            messages = listOf(senseiResponse) + it.messages,
                            isTyping = false
                        )
                    }
                    
                    // Trigger playback if available AND enabled
                    if (_uiState.value.isVoiceEnabled) {
                        response.videoBase64?.let { 
                            _videoEvents.emit(it)
                            _uiState.update { state -> state.copy(currentVideoBase64 = it) }
                        } ?: response.audioBase64?.let { 
                            _audioEvents.emit(it)
                        }
                    }
                }.onFailure { error ->
                    val errorMsg = Message(
                        id = System.currentTimeMillis().toString(),
                        text = "Désolé, une erreur de connexion s'est produite :(",
                        isFromUser = false
                    )
                    _uiState.update { 
                        it.copy(
                            messages = listOf(errorMsg) + it.messages,
                            isTyping = false
                        )
                    }
                }
            }
        }
    }
}
