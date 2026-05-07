package com.example.sensai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.local.TokenManager
import com.example.sensai.data.model.LoginRequest
import com.example.sensai.data.model.RegisterRequest
import com.example.sensai.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.login(LoginRequest(email, pass)).onSuccess { response ->
                tokenManager.saveTokens(response.accessToken, response.refreshToken, response.username, response.userId)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Erreur de connexion")
            }
        }
    }

    fun register(email: String, user: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.register(RegisterRequest(email, user, pass)).onSuccess { response ->
                tokenManager.saveTokens(response.accessToken, response.refreshToken, response.username, response.userId)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Erreur d'inscription")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAll()
            _uiState.value = AuthUiState.Idle
        }
    }
}
