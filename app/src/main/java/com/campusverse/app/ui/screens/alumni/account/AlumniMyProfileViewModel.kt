package com.campusverse.app.ui.screens.alumni.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.AlumniProfileData
import com.campusverse.app.data.repository.NetworkAlumniRepository
import com.campusverse.app.domain.alumni.AlumniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlumniMyProfileUiState {
    data object Loading : AlumniMyProfileUiState
    data class Success(val profile: AlumniProfileData, val isSaving: Boolean = false, val saveMessage: String? = null) : AlumniMyProfileUiState
    data class Error(val message: String) : AlumniMyProfileUiState
}

class AlumniMyProfileViewModel(
    private val repository: AlumniRepository = NetworkAlumniRepository.instance,
    private val userProfileRepository: com.campusverse.app.data.repository.UserProfileRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlumniMyProfileUiState>(AlumniMyProfileUiState.Loading)
    val uiState: StateFlow<AlumniMyProfileUiState> = _uiState.asStateFlow()

    init {
        loadMyProfile()
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _uiState.value = AlumniMyProfileUiState.Loading
            repository.getMyProfile()
                .onSuccess { profile ->
                    _uiState.value = AlumniMyProfileUiState.Success(profile)
                }
                .onFailure { err ->
                    _uiState.value = AlumniMyProfileUiState.Error(err.message ?: "Failed to load profile.")
                }
        }
    }

    fun saveProfile(updated: AlumniProfileData) {
        val current = _uiState.value as? AlumniMyProfileUiState.Success ?: return

        if (updated.fullName.isBlank()) {
            _uiState.value = current.copy(isSaving = false, saveMessage = "Full Name cannot be empty.")
            return
        }
        if (updated.company.isBlank()) {
            _uiState.value = current.copy(isSaving = false, saveMessage = "Company Name cannot be empty.")
            return
        }
        if (updated.designation.isBlank()) {
            _uiState.value = current.copy(isSaving = false, saveMessage = "Designation cannot be empty.")
            return
        }

        _uiState.value = current.copy(isSaving = true, saveMessage = null)

        viewModelScope.launch {
            repository.updateAlumniProfile(updated)
                .onSuccess { saved ->
                    try {
                        userProfileRepository?.updateProfile(
                            com.campusverse.app.data.repository.CurrentUserProfile(
                                userId = saved.userId,
                                name = saved.fullName,
                                email = saved.email,
                                photoUrl = saved.avatarUrl,
                                role = com.campusverse.app.data.model.UserRole.ALUMNI,
                                profileData = saved
                            )
                        )
                    } catch (_: Exception) {}
                    _uiState.value = AlumniMyProfileUiState.Success(saved, isSaving = false, saveMessage = "Profile updated successfully!")
                }
                .onFailure { err ->
                    _uiState.value = current.copy(isSaving = false, saveMessage = err.message ?: "Update failed.")
                }
        }
    }
}
