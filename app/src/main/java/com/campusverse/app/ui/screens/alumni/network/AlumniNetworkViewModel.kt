package com.campusverse.app.ui.screens.alumni.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.AlumniConnectionItem
import com.campusverse.app.data.model.AlumniConnectionRequestItem
import com.campusverse.app.data.model.AlumniProfileData
import com.campusverse.app.data.repository.NetworkAlumniRepository
import com.campusverse.app.domain.alumni.AlumniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlumniNetworkUiState {
    data object Loading : AlumniNetworkUiState
    data class Success(
        val alumniList: List<AlumniProfileData>,
        val connections: List<AlumniConnectionItem>,
        val pendingRequests: List<AlumniConnectionRequestItem>,
        val searchQuery: String = "",
        val selectedCompany: String? = null,
        val filterMentor: Boolean = false,
        val filterReferral: Boolean = false
    ) : AlumniNetworkUiState
    data class Error(val message: String) : AlumniNetworkUiState
}

class AlumniNetworkViewModel(
    private val repository: AlumniRepository = NetworkAlumniRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlumniNetworkUiState>(AlumniNetworkUiState.Loading)
    val uiState: StateFlow<AlumniNetworkUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentCompany: String? = null
    private var currentMentorFilter: Boolean = false
    private var currentReferralFilter: Boolean = false

    init {
        loadNetworkData()
    }

    fun loadNetworkData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AlumniNetworkUiState.Success) {
                _uiState.value = AlumniNetworkUiState.Loading
            }
            val alumniResult = repository.getAlumni(
                search = currentSearch.ifBlank { null },
                company = currentCompany,
                willingToMentor = if (currentMentorFilter) true else null,
                willingToRefer = if (currentReferralFilter) true else null
            )
            val connsResult = repository.getConnections()

            if (alumniResult.isSuccess) {
                val (conns, reqs) = connsResult.getOrDefault(Pair(emptyList(), emptyList()))
                _uiState.value = AlumniNetworkUiState.Success(
                    alumniList = alumniResult.getOrDefault(emptyList()),
                    connections = conns,
                    pendingRequests = reqs,
                    searchQuery = currentSearch,
                    selectedCompany = currentCompany,
                    filterMentor = currentMentorFilter,
                    filterReferral = currentReferralFilter
                )
            } else if (_uiState.value !is AlumniNetworkUiState.Success) {
                _uiState.value = AlumniNetworkUiState.Error(
                    alumniResult.exceptionOrNull()?.message ?: "Failed to load alumni network."
                )
            }
        }
    }

    fun onSearchChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? AlumniNetworkUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadNetworkData(showLoading = false)
    }

    fun toggleMentorFilter() {
        currentMentorFilter = !currentMentorFilter
        loadNetworkData()
    }

    fun toggleReferralFilter() {
        currentReferralFilter = !currentReferralFilter
        loadNetworkData()
    }

    fun selectCompany(company: String?) {
        currentCompany = if (currentCompany == company) null else company
        loadNetworkData()
    }

    fun connect(alumniId: String) {
        viewModelScope.launch {
            repository.connectWithAlumni(alumniId)
            loadNetworkData()
        }
    }

    fun respondToRequest(connectionId: String, status: String) {
        viewModelScope.launch {
            repository.respondToConnectionRequest(connectionId, status)
            loadNetworkData()
        }
    }

    fun deleteConnection(connectionId: String) {
        viewModelScope.launch {
            repository.deleteConnection(connectionId)
            loadNetworkData()
        }
    }

    fun toggleSaveAlumni(alumni: AlumniProfileData) {
        viewModelScope.launch {
            if (alumni.isSaved) {
                repository.unsaveAlumni(alumni.userId)
            } else {
                repository.saveAlumni(alumni.userId)
            }
            loadNetworkData()
        }
    }
}
