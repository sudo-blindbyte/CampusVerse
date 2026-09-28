package com.campusverse.app.ui.screens.alumni.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.CampusEvent
import com.campusverse.app.data.repository.NetworkAlumniRepository
import com.campusverse.app.domain.alumni.AlumniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlumniEventsUiState {
    data object Loading : AlumniEventsUiState
    data class Success(
        val events: List<CampusEvent>,
        val searchQuery: String = "",
        val selectedCategory: String? = null
    ) : AlumniEventsUiState
    data class Error(val message: String) : AlumniEventsUiState
}

class AlumniEventsViewModel(
    private val repository: AlumniRepository = NetworkAlumniRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlumniEventsUiState>(AlumniEventsUiState.Loading)
    val uiState: StateFlow<AlumniEventsUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentCategory: String? = null

    init {
        loadEvents()
    }

    fun loadEvents(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AlumniEventsUiState.Success) {
                _uiState.value = AlumniEventsUiState.Loading
            }
            repository.getEvents(
                search = currentSearch.ifBlank { null },
                category = currentCategory
            )
                .onSuccess { events ->
                    _uiState.value = AlumniEventsUiState.Success(
                        events = events,
                        searchQuery = currentSearch,
                        selectedCategory = currentCategory
                    )
                }
                .onFailure { err ->
                    if (_uiState.value !is AlumniEventsUiState.Success) {
                        _uiState.value = AlumniEventsUiState.Error(err.message ?: "Failed to load events.")
                    }
                }
        }
    }

    fun onSearchChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? AlumniEventsUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadEvents(showLoading = false)
    }

    fun selectCategory(category: String?) {
        currentCategory = if (currentCategory == category) null else category
        loadEvents()
    }

    fun toggleRegistration(event: CampusEvent) {
        viewModelScope.launch {
            if (event.isRegistered) {
                repository.unregisterFromEvent(event.id)
            } else {
                repository.registerForEvent(event.id)
            }
            loadEvents()
        }
    }
}
