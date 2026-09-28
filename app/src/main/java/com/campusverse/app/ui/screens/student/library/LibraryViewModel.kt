package com.campusverse.app.ui.screens.student.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.LibraryResource
import com.campusverse.app.data.repository.NetworkStudentRepository
import com.campusverse.app.domain.student.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Success(
        val resources: List<LibraryResource>,
        val selectedCategory: String = "ALL",
        val searchQuery: String = ""
    ) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel(
    private val repository: StudentRepository = NetworkStudentRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary(category: String = "ALL", search: String? = null, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is LibraryUiState.Success) {
                _uiState.value = LibraryUiState.Loading
            }
            try {
                val catParam = if (category == "ALL") null else category
                val result = repository.getLibraryResources(search = search, category = catParam)
                val list = result.getOrDefault(emptyList())
                _uiState.value = LibraryUiState.Success(
                    resources = list,
                    selectedCategory = category,
                    searchQuery = search ?: ""
                )
            } catch (e: Exception) {
                if (_uiState.value !is LibraryUiState.Success) {
                    _uiState.value = LibraryUiState.Error(e.message ?: "Failed to load library resources.")
                }
            }
        }
    }

    fun setCategory(category: String) {
        val currSearch = (_uiState.value as? LibraryUiState.Success)?.searchQuery
        loadLibrary(category = category, search = currSearch)
    }

    fun searchLibrary(query: String) {
        val currState = _uiState.value as? LibraryUiState.Success
        if (currState != null) {
            _uiState.value = currState.copy(searchQuery = query)
        }
        val currCat = currState?.selectedCategory ?: "ALL"
        loadLibrary(category = currCat, search = query.ifBlank { null }, showLoading = false)
    }
}
