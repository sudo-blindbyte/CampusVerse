package com.campusverse.app.ui.screens.student.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.CampusEvent
import com.campusverse.app.data.repository.NetworkStudentRepository
import com.campusverse.app.domain.student.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Success(
        val events: List<CampusEvent>,
        val selectedCategory: String = "ALL",
        val searchQuery: String = "",
        val toastMessage: String? = null
    ) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

class EventsViewModel(
    private val repository: StudentRepository = NetworkStudentRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents(category: String = "ALL", search: String? = null, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is EventsUiState.Success) {
                _uiState.value = EventsUiState.Loading
            }
            try {
                val catParam = if (category == "ALL") null else category
                val result = repository.getEvents(search = search, category = catParam)
                val list = result.getOrDefault(emptyList())
                _uiState.value = EventsUiState.Success(
                    events = list,
                    selectedCategory = category,
                    searchQuery = search ?: ""
                )
            } catch (e: Exception) {
                if (_uiState.value !is EventsUiState.Success) {
                    _uiState.value = EventsUiState.Error(e.message ?: "Failed to load events.")
                }
            }
        }
    }

    fun toggleRegistration(event: CampusEvent) {
        viewModelScope.launch {
            val currState = _uiState.value as? EventsUiState.Success ?: return@launch
            if (event.isRegistered) {
                repository.unregisterFromEvent(event.id)
                val updated = currState.events.map {
                    if (it.id == event.id) it.copy(isRegistered = false, registeredCount = (it.registeredCount - 1).coerceAtLeast(0)) else it
                }
                _uiState.value = currState.copy(events = updated, toastMessage = "Unregistered from ${event.title}")
            } else {
                repository.registerForEvent(event.id)
                val updated = currState.events.map {
                    if (it.id == event.id) it.copy(isRegistered = true, registeredCount = it.registeredCount + 1) else it
                }
                _uiState.value = currState.copy(events = updated, toastMessage = "Registered for ${event.title}!")
            }
        }
    }

    fun setCategory(category: String) {
        val currSearch = (_uiState.value as? EventsUiState.Success)?.searchQuery
        loadEvents(category = category, search = currSearch)
    }

    fun searchEvents(query: String) {
        val currState = _uiState.value as? EventsUiState.Success
        if (currState != null) {
            _uiState.value = currState.copy(searchQuery = query)
        }
        val currCat = currState?.selectedCategory ?: "ALL"
        loadEvents(category = currCat, search = query.ifBlank { null }, showLoading = false)
    }

    fun clearToast() {
        val currState = _uiState.value as? EventsUiState.Success ?: return
        _uiState.value = currState.copy(toastMessage = null)
    }
}
