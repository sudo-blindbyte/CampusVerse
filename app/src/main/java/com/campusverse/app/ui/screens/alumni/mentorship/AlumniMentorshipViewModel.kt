package com.campusverse.app.ui.screens.alumni.mentorship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.MentorItem
import com.campusverse.app.data.model.MentorshipRequestItem
import com.campusverse.app.data.model.MentorshipSessionItem
import com.campusverse.app.data.repository.NetworkAlumniRepository
import com.campusverse.app.domain.alumni.AlumniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlumniMentorshipUiState {
    data object Loading : AlumniMentorshipUiState
    data class Success(
        val mentors: List<MentorItem>,
        val sessions: List<MentorshipSessionItem>,
        val requests: List<MentorshipRequestItem>,
        val searchQuery: String = "",
        val selectedExpertise: String? = null
    ) : AlumniMentorshipUiState
    data class Error(val message: String) : AlumniMentorshipUiState
}

class AlumniMentorshipViewModel(
    private val repository: AlumniRepository = NetworkAlumniRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlumniMentorshipUiState>(AlumniMentorshipUiState.Loading)
    val uiState: StateFlow<AlumniMentorshipUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentExpertise: String? = null

    init {
        loadMentorshipData()
    }

    fun loadMentorshipData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AlumniMentorshipUiState.Success) {
                _uiState.value = AlumniMentorshipUiState.Loading
            }
            val mentorsRes = repository.getMentors(
                search = currentSearch.ifBlank { null },
                expertise = currentExpertise
            )
            val sessionsRes = repository.getMentorshipSessions()
            val requestsRes = repository.getMentorshipRequests()

            if (mentorsRes.isSuccess) {
                _uiState.value = AlumniMentorshipUiState.Success(
                    mentors = mentorsRes.getOrDefault(emptyList()),
                    sessions = sessionsRes.getOrDefault(emptyList()),
                    requests = requestsRes.getOrDefault(emptyList()),
                    searchQuery = currentSearch,
                    selectedExpertise = currentExpertise
                )
            } else if (_uiState.value !is AlumniMentorshipUiState.Success) {
                _uiState.value = AlumniMentorshipUiState.Error(
                    mentorsRes.exceptionOrNull()?.message ?: "Failed to load mentorship hub."
                )
            }
        }
    }

    fun onSearchChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? AlumniMentorshipUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadMentorshipData(showLoading = false)
    }

    fun selectExpertise(expertise: String?) {
        currentExpertise = if (currentExpertise == expertise) null else expertise
        loadMentorshipData()
    }

    fun respondRequest(requestId: String, status: String) {
        viewModelScope.launch {
            repository.respondMentorshipRequest(requestId, status)
            loadMentorshipData()
        }
    }
}
