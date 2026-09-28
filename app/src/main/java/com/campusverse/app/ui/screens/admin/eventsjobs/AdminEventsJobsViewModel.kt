package com.campusverse.app.ui.screens.admin.eventsjobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.AdminEventItem
import com.campusverse.app.data.model.AdminJobItem
import com.campusverse.app.data.repository.NetworkAdminRepository
import com.campusverse.app.domain.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminEventsJobsUiState {
    object Loading : AdminEventsJobsUiState
    data class Success(
        val events: List<AdminEventItem>,
        val jobs: List<AdminJobItem>,
        val selectedTab: Int = 0, // 0 = Events, 1 = Jobs
        val feedbackMessage: String? = null
    ) : AdminEventsJobsUiState
    data class Error(val message: String) : AdminEventsJobsUiState
}

class AdminEventsJobsViewModel(
    private val repository: AdminRepository = NetworkAdminRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminEventsJobsUiState>(AdminEventsJobsUiState.Loading)
    val uiState: StateFlow<AdminEventsJobsUiState> = _uiState.asStateFlow()

    private var currentTab = 0

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = AdminEventsJobsUiState.Loading
            val eventsRes = repository.getEvents()
            val jobsRes = repository.getJobs()

            if (eventsRes.isSuccess && jobsRes.isSuccess) {
                _uiState.value = AdminEventsJobsUiState.Success(
                    events = eventsRes.getOrDefault(emptyList()),
                    jobs = jobsRes.getOrDefault(emptyList()),
                    selectedTab = currentTab
                )
            } else {
                _uiState.value = AdminEventsJobsUiState.Error("Failed to load opportunities")
            }
        }
    }

    fun selectTab(tab: Int) {
        currentTab = tab
        val current = _uiState.value
        if (current is AdminEventsJobsUiState.Success) {
            _uiState.value = current.copy(selectedTab = tab)
        }
    }

    fun moderateEvent(eventId: String, action: String, reason: String? = null) {
        viewModelScope.launch {
            repository.moderateEvent(eventId, action, reason)
                .onSuccess {
                    val current = _uiState.value
                    if (current is AdminEventsJobsUiState.Success) {
                        val updated = if (action == "REMOVE") {
                            current.events.filterNot { it.id == eventId }
                        } else {
                            current.events.map { if (it.id == eventId) it.copy(status = if (action == "APPROVE") "UPCOMING" else "CANCELLED") else it }
                        }
                        _uiState.value = current.copy(events = updated, feedbackMessage = "Event $action successfully.")
                    }
                }
        }
    }

    fun moderateJob(jobId: String, action: String, reason: String? = null) {
        viewModelScope.launch {
            repository.moderateJob(jobId, action, reason)
                .onSuccess {
                    val current = _uiState.value
                    if (current is AdminEventsJobsUiState.Success) {
                        val updated = if (action == "REMOVE") {
                            current.jobs.filterNot { it.id == jobId }
                        } else {
                            current.jobs.map { if (it.id == jobId) it.copy(status = if (action == "APPROVE") "ACTIVE" else "CLOSED") else it }
                        }
                        _uiState.value = current.copy(jobs = updated, feedbackMessage = "Job listing $action successfully.")
                    }
                }
        }
    }

    fun createEvent(title: String, eventType: String, location: String, organizerName: String, organizerEmail: String, onSuccess: () -> Unit) {
        if (title.isBlank() || location.isBlank()) return
        viewModelScope.launch {
            val newEvent = AdminEventItem(
                id = "e_${System.currentTimeMillis()}",
                title = title,
                eventType = eventType,
                date = "2026-10-01T10:00:00Z",
                location = location,
                status = "UPCOMING",
                organizerName = organizerName.ifBlank { "Campus Admin" },
                organizerEmail = organizerEmail.ifBlank { "admin@campusverse.edu" }
            )
            val current = _uiState.value
            if (current is AdminEventsJobsUiState.Success) {
                _uiState.value = current.copy(
                    events = listOf(newEvent) + current.events,
                    feedbackMessage = "Event '$title' created successfully."
                )
            }
            onSuccess()
        }
    }

    fun createJob(title: String, companyName: String, jobType: String, location: String, posterName: String, posterEmail: String, onSuccess: () -> Unit) {
        if (title.isBlank() || companyName.isBlank()) return
        viewModelScope.launch {
            val newJob = AdminJobItem(
                id = "j_${System.currentTimeMillis()}",
                title = title,
                companyName = companyName,
                jobType = jobType,
                location = location.ifBlank { "Remote" },
                status = "ACTIVE",
                posterName = posterName.ifBlank { "Campus Admin" },
                posterEmail = posterEmail.ifBlank { "admin@campusverse.edu" }
            )
            val current = _uiState.value
            if (current is AdminEventsJobsUiState.Success) {
                _uiState.value = current.copy(
                    jobs = listOf(newJob) + current.jobs,
                    feedbackMessage = "Job listing '$title' created successfully."
                )
            }
            onSuccess()
        }
    }
}
