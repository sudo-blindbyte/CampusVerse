package com.campusverse.app.ui.screens.aspirant.scholarships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.ScholarshipItem
import com.campusverse.app.data.repository.NetworkAspirantRepository
import com.campusverse.app.domain.aspirant.AspirantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScholarshipFinderUiState {
    data object Loading : ScholarshipFinderUiState
    data class Success(
        val scholarships: List<ScholarshipItem>,
        val searchQuery: String = "",
        val selectedCategory: String? = null
    ) : ScholarshipFinderUiState
    data class Error(val message: String) : ScholarshipFinderUiState
}

class ScholarshipFinderViewModel(
    private val repository: AspirantRepository = NetworkAspirantRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScholarshipFinderUiState>(ScholarshipFinderUiState.Loading)
    val uiState: StateFlow<ScholarshipFinderUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentCategory: String? = null

    init {
        loadScholarships()
    }

    fun loadScholarships(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is ScholarshipFinderUiState.Success) {
                _uiState.value = ScholarshipFinderUiState.Loading
            }
            repository.getScholarships(
                search = currentSearch.ifBlank { null },
                category = currentCategory
            ).onSuccess { list ->
                _uiState.value = ScholarshipFinderUiState.Success(
                    scholarships = list,
                    searchQuery = currentSearch,
                    selectedCategory = currentCategory
                )
            }.onFailure { err ->
                if (_uiState.value !is ScholarshipFinderUiState.Success) {
                    _uiState.value = ScholarshipFinderUiState.Error(err.message ?: "Failed to load scholarships")
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? ScholarshipFinderUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadScholarships(showLoading = false)
    }

    fun onCategorySelected(category: String?) {
        currentCategory = category
        loadScholarships()
    }

    fun toggleSaveScholarship(id: String, currentSaved: Boolean) {
        viewModelScope.launch {
            if (currentSaved) {
                repository.unsaveScholarship(id)
            } else {
                repository.saveScholarship(id)
            }
            loadScholarships()
        }
    }
}
