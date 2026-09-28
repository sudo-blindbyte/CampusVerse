package com.campusverse.app.ui.screens.admin.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.AdminMarketplaceItem
import com.campusverse.app.data.repository.NetworkAdminRepository
import com.campusverse.app.domain.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminMarketplaceUiState {
    object Loading : AdminMarketplaceUiState
    data class Success(
        val items: List<AdminMarketplaceItem>,
        val searchQuery: String = "",
        val selectedCategory: String = "ALL",
        val reviewingItem: AdminMarketplaceItem? = null,
        val feedbackMessage: String? = null
    ) : AdminMarketplaceUiState
    data class Error(val message: String) : AdminMarketplaceUiState
}

class AdminMarketplaceViewModel(
    private val repository: AdminRepository = NetworkAdminRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminMarketplaceUiState>(AdminMarketplaceUiState.Loading)
    val uiState: StateFlow<AdminMarketplaceUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentCategory: String = "ALL"

    init {
        loadMarketplace()
    }

    fun loadMarketplace(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AdminMarketplaceUiState.Success) {
                _uiState.value = AdminMarketplaceUiState.Loading
            }
            repository.getMarketplaceListings(search = currentSearch, category = currentCategory)
                .onSuccess { list ->
                    _uiState.value = AdminMarketplaceUiState.Success(
                        items = list,
                        searchQuery = currentSearch,
                        selectedCategory = currentCategory
                    )
                }
                .onFailure { err ->
                    if (_uiState.value !is AdminMarketplaceUiState.Success) {
                        _uiState.value = AdminMarketplaceUiState.Error(err.message ?: "Failed to load listings")
                    }
                }
        }
    }

    fun onSearchChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? AdminMarketplaceUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadMarketplace(showLoading = false)
    }

    fun onCategorySelected(cat: String) {
        currentCategory = cat
        loadMarketplace()
    }

    fun onStartReview(item: AdminMarketplaceItem?) {
        val current = _uiState.value
        if (current is AdminMarketplaceUiState.Success) {
            _uiState.value = current.copy(reviewingItem = item, feedbackMessage = null)
        }
    }

    fun moderateItem(itemId: String, action: String, reason: String? = null) {
        viewModelScope.launch {
            repository.moderateMarketplaceItem(itemId, action, reason)
                .onSuccess {
                    val current = _uiState.value
                    if (current is AdminMarketplaceUiState.Success) {
                        val updatedList = if (action == "REMOVE") {
                            current.items.filterNot { it.id == itemId }
                        } else {
                            current.items.map { if (it.id == itemId) it.copy(status = if (action == "APPROVE") "AVAILABLE" else "FLAGGED") else it }
                        }
                        _uiState.value = current.copy(
                            items = updatedList,
                            reviewingItem = null,
                            feedbackMessage = "Listing ${action.lowercase()}d successfully."
                        )
                    }
                }
                .onFailure { err ->
                    val current = _uiState.value
                    if (current is AdminMarketplaceUiState.Success) {
                        _uiState.value = current.copy(feedbackMessage = "Error: ${err.message}")
                    }
                }
        }
    }
}
