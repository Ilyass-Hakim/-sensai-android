package com.example.sensai.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.network.ApiService
import com.example.sensai.data.network.dto.UpdateProfileDto
import com.example.sensai.data.network.dto.UserProfileDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val profile: UserProfileDto? = null,
    val error: String? = null,
    val successMessage: String? = null,
    
    // Editable fields
    val username: String = "",
    val bio: String = "",
    val preferredGenres: List<String> = emptyList(),
    
    // Available genres
    val availableGenres: List<String> = listOf("Action", "Romance", "Fantasy", "Sci-Fi", "Comedy", "Slice of Life", "Horror", "Sports", "Mecha", "Isekai")
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val profile = apiService.getProfile()
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        profile = profile,
                        username = profile.username,
                        bio = profile.bio ?: "",
                        preferredGenres = profile.preferredGenres ?: emptyList()
                    ) 
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load profile") }
            }
        }
    }

    fun updateUsername(name: String) {
        _state.update { it.copy(username = name, successMessage = null, error = null) }
    }

    fun updateBio(bio: String) {
        _state.update { it.copy(bio = bio, successMessage = null, error = null) }
    }

    fun toggleGenre(genre: String) {
        _state.update { state ->
            val current = state.preferredGenres.toMutableList()
            if (current.contains(genre)) {
                current.remove(genre)
            } else {
                current.add(genre)
            }
            state.copy(preferredGenres = current, successMessage = null, error = null)
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val request = UpdateProfileDto(
                    username = _state.value.username,
                    bio = _state.value.bio,
                    preferredGenres = _state.value.preferredGenres
                )
                val updatedProfile = apiService.updateProfile(request)
                _state.update { 
                    it.copy(
                        isSaving = false, 
                        profile = updatedProfile,
                        username = updatedProfile.username,
                        bio = updatedProfile.bio ?: "",
                        preferredGenres = updatedProfile.preferredGenres ?: emptyList(),
                        successMessage = "Profile saved successfully!"
                    ) 
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save profile") }
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val file = getFileFromUri(uri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    
                    val updatedProfile = apiService.uploadAvatar(body)
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            profile = updatedProfile,
                            successMessage = "Avatar updated successfully!"
                        ) 
                    }
                    
                    // Clean up temp file
                    file.delete()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to process image") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to upload avatar") }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("avatar_upload", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            
            inputStream.copyTo(outputStream)
            
            inputStream.close()
            outputStream.close()
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
