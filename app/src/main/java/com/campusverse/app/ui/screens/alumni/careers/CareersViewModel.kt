package com.campusverse.app.ui.screens.alumni.careers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.CompanyItem
import com.campusverse.app.data.model.JobOpportunity
import com.campusverse.app.data.repository.NetworkAlumniRepository
import com.campusverse.app.domain.alumni.AlumniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CareersUiState {
    data object Loading : CareersUiState
    data class Success(
        val jobs: List<JobOpportunity>,
        val recommendedJobs: List<JobOpportunity>,
        val companies: List<CompanyItem>,
        val savedCount: Int = 0,
        val applicationsCount: Int = 0,
        val searchQuery: String = "",
        val selectedRoleType: String? = null,
        val isRemoteOnly: Boolean = false
    ) : CareersUiState
    data class Error(val message: String) : CareersUiState
}

class CareersViewModel(
    private val repository: AlumniRepository = NetworkAlumniRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<CareersUiState>(CareersUiState.Loading)
    val uiState: StateFlow<CareersUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentRoleType: String? = null
    private var currentRemoteOnly: Boolean = false

    init {
        loadCareersData()
    }

    fun loadCareersData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is CareersUiState.Success) {
                _uiState.value = CareersUiState.Loading
            }
            val jobsRes = repository.getJobs(
                search = currentSearch.ifBlank { null },
                roleType = currentRoleType,
                isRemote = if (currentRemoteOnly) true else null
            )
            val recommendedRes = repository.getRecommendedJobs()
            val companiesRes = repository.getCompanies()
            val savedRes = repository.getSavedJobs()
            val appsRes = repository.getApplications()

            if (jobsRes.isSuccess) {
                _uiState.value = CareersUiState.Success(
                    jobs = jobsRes.getOrDefault(emptyList()),
                    recommendedJobs = recommendedRes.getOrDefault(emptyList()),
                    companies = companiesRes.getOrDefault(emptyList()),
                    savedCount = savedRes.getOrDefault(emptyList()).size,
                    applicationsCount = appsRes.getOrDefault(emptyList()).size,
                    searchQuery = currentSearch,
                    selectedRoleType = currentRoleType,
                    isRemoteOnly = currentRemoteOnly
                )
            } else if (_uiState.value !is CareersUiState.Success) {
                _uiState.value = CareersUiState.Error(
                    jobsRes.exceptionOrNull()?.message ?: "Failed to load careers."
                )
            }
        }
    }

    fun onSearchChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? CareersUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadCareersData(showLoading = false)
    }

    fun selectRoleType(roleType: String?) {
        currentRoleType = if (currentRoleType == roleType) null else roleType
        loadCareersData()
    }

    fun toggleRemoteOnly() {
        currentRemoteOnly = !currentRemoteOnly
        loadCareersData()
    }

    fun toggleSaveJob(jobId: String, currentlySaved: Boolean) {
        viewModelScope.launch {
            if (currentlySaved) {
                repository.unsaveJob(jobId)
            } else {
                repository.saveJob(jobId)
            }
            loadCareersData()
        }
    }
}
