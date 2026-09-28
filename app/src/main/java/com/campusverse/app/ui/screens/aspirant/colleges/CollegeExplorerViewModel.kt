package com.campusverse.app.ui.screens.aspirant.colleges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.CollegeItem
import com.campusverse.app.data.repository.NetworkAspirantRepository
import com.campusverse.app.domain.aspirant.AspirantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CollegeExplorerUiState {
    data object Loading : CollegeExplorerUiState
    data class Success(
        val colleges: List<CollegeItem>,
        val searchQuery: String = "",
        val selectedCountry: String? = null,
        val selectedDegree: String? = null,
        val selectedSortBy: String = "ranking"
    ) : CollegeExplorerUiState
    data class Error(val message: String) : CollegeExplorerUiState
}

class CollegeExplorerViewModel(
    private val repository: AspirantRepository = NetworkAspirantRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollegeExplorerUiState>(CollegeExplorerUiState.Loading)
    val uiState: StateFlow<CollegeExplorerUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentCountry: String? = null
    private var currentDegree: String? = null
    private var currentSort: String = "ranking"

    init {
        loadColleges()
    }

    fun loadColleges(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is CollegeExplorerUiState.Success) {
                _uiState.value = CollegeExplorerUiState.Loading
            }
            repository.getColleges(
                search = currentSearch.ifBlank { null },
                country = currentCountry,
                degree = currentDegree,
                sortBy = currentSort
            ).onSuccess { list ->
                _uiState.value = CollegeExplorerUiState.Success(
                    colleges = list,
                    searchQuery = currentSearch,
                    selectedCountry = currentCountry,
                    selectedDegree = currentDegree,
                    selectedSortBy = currentSort
                )
            }.onFailure { err ->
                if (_uiState.value !is CollegeExplorerUiState.Success) {
                    _uiState.value = CollegeExplorerUiState.Error(err.message ?: "Failed to load colleges")
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? CollegeExplorerUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadColleges(showLoading = false)
    }

    fun onCountrySelected(country: String?) {
        currentCountry = country
        loadColleges()
    }

    fun onDegreeSelected(degree: String?) {
        currentDegree = degree
        loadColleges()
    }

    fun onSortChanged(sortBy: String) {
        currentSort = sortBy
        loadColleges()
    }

    fun toggleSaveCollege(collegeId: String, currentSaved: Boolean) {
        viewModelScope.launch {
            if (currentSaved) {
                repository.unsaveCollege(collegeId)
            } else {
                repository.saveCollege(collegeId)
            }
            loadColleges()
        }
    }
}
