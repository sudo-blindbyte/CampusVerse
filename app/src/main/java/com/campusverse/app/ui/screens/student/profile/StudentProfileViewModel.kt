package com.campusverse.app.ui.screens.student.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.StudentProfileData
import com.campusverse.app.data.repository.CurrentUserProfile
import com.campusverse.app.data.repository.NetworkStudentRepository
import com.campusverse.app.data.repository.UserProfileRepository
import com.campusverse.app.domain.student.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StudentProfileUiState {
    data object Loading : StudentProfileUiState
    data class Success(
        val profile: StudentProfileData,
        val isSaving: Boolean = false,
        val saveSuccessMessage: String? = null,
        val errorMessage: String? = null
    ) : StudentProfileUiState
    data class Error(val message: String) : StudentProfileUiState
}

class StudentProfileViewModel(
    private val repository: StudentRepository = NetworkStudentRepository.instance,
    private val userProfileRepository: UserProfileRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentProfileUiState>(StudentProfileUiState.Loading)
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = StudentProfileUiState.Loading
            try {
                val result = repository.getStudentProfile()
                val p = result.getOrNull()
                if (p != null) {
                    _uiState.value = StudentProfileUiState.Success(profile = p)
                } else {
                    _uiState.value = StudentProfileUiState.Error("Failed to load profile data.")
                }
            } catch (e: Exception) {
                _uiState.value = StudentProfileUiState.Error(e.message ?: "Failed to load profile.")
            }
        }
    }

    fun saveProfile(
        fullName: String,
        bio: String?,
        location: String?,
        degree: String,
        branch: String,
        semesterText: String,
        cgpaText: String,
        skills: List<String>
    ) {
        val currState = _uiState.value as? StudentProfileUiState.Success ?: return

        // 1. Full Name Validation
        val trimmedName = fullName.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = currState.copy(errorMessage = "Full Name cannot be blank.")
            return
        }
        if (trimmedName.length < 2) {
            _uiState.value = currState.copy(errorMessage = "Full Name must be at least 2 characters long.")
            return
        }
        if (!trimmedName.all { it.isLetter() || it.isWhitespace() || it == '.' || it == '-' || it == '\'' }) {
            _uiState.value = currState.copy(errorMessage = "Full Name contains invalid special characters.")
            return
        }

        // 2. Degree Validation
        val trimmedDegree = degree.trim()
        if (trimmedDegree.isBlank()) {
            _uiState.value = currState.copy(errorMessage = "Degree / Program cannot be blank.")
            return
        }

        // 3. Major / Department Validation
        val trimmedBranch = branch.trim()
        if (trimmedBranch.isBlank()) {
            _uiState.value = currState.copy(errorMessage = "Major / Department cannot be blank.")
            return
        }

        // 4. Semester Validation
        val sem = semesterText.trim().toIntOrNull()
        if (sem == null || sem < 1 || sem > 12) {
            _uiState.value = currState.copy(errorMessage = "Please enter a valid semester number between 1 and 12.")
            return
        }

        // 5. CGPA Validation
        val cg = cgpaText.trim().toDoubleOrNull()
        if (cg == null || cg < 0.0 || cg > 10.0) {
            _uiState.value = currState.copy(errorMessage = "Please enter a valid CGPA between 0.00 and 10.00.")
            return
        }

        val updatedProfile = currState.profile.copy(
            fullName = trimmedName,
            bio = bio?.trim(),
            location = location?.trim(),
            degree = trimmedDegree,
            branch = trimmedBranch,
            semester = sem,
            cgpa = cg,
            skills = skills
        )

        _uiState.value = currState.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val result = repository.updateStudentProfile(updatedProfile)
                if (result.isSuccess) {
                    try {
                        userProfileRepository?.updateProfile(
                            CurrentUserProfile(
                                userId = updatedProfile.userId,
                                name = updatedProfile.fullName,
                                email = updatedProfile.email,
                                photoUrl = updatedProfile.avatarUrl,
                                role = com.campusverse.app.data.model.UserRole.STUDENT,
                                profileData = updatedProfile
                            )
                        )
                    } catch (_: Exception) {}

                    _uiState.value = StudentProfileUiState.Success(
                        profile = updatedProfile,
                        isSaving = false,
                        saveSuccessMessage = "Student profile saved successfully!"
                    )
                } else {
                    _uiState.value = currState.copy(
                        isSaving = false,
                        errorMessage = "Failed to update profile on backend."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currState.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Error saving profile."
                )
            }
        }
    }

    fun clearMessages() {
        val curr = _uiState.value as? StudentProfileUiState.Success ?: return
        _uiState.value = curr.copy(saveSuccessMessage = null, errorMessage = null)
    }
}
