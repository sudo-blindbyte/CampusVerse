package com.campusverse.app.ui.screens.admin.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.NoteItem
import com.campusverse.app.data.repository.NetworkAdminRepository
import com.campusverse.app.domain.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminNotesUiState {
    data object Loading : AdminNotesUiState
    data class Success(
        val notes: List<NoteItem>,
        val selectedStatus: String = "ALL",
        val searchQuery: String = "",
        val feedbackMessage: String? = null
    ) : AdminNotesUiState
    data class Error(val message: String) : AdminNotesUiState
}

class AdminNotesViewModel(
    private val repository: AdminRepository = NetworkAdminRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminNotesUiState>(AdminNotesUiState.Loading)
    val uiState: StateFlow<AdminNotesUiState> = _uiState.asStateFlow()

    private var currentStatus: String = "ALL"
    private var currentSearch: String = ""

    init {
        loadNotes()
    }

    fun loadNotes(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AdminNotesUiState.Success) {
                _uiState.value = AdminNotesUiState.Loading
            }
            val statusParam = if (currentStatus == "ALL") null else currentStatus
            val searchParam = currentSearch.ifBlank { null }
            repository.getAdminNotes(status = statusParam, search = searchParam)
                .onSuccess { list ->
                    _uiState.value = AdminNotesUiState.Success(
                        notes = list,
                        selectedStatus = currentStatus,
                        searchQuery = currentSearch
                    )
                }
                .onFailure { err ->
                    if (_uiState.value !is AdminNotesUiState.Success) {
                        _uiState.value = AdminNotesUiState.Error(err.message ?: "Failed to load admin notes")
                    }
                }
        }
    }

    fun onStatusFilterSelected(status: String) {
        currentStatus = status
        loadNotes()
    }

    fun searchNotes(query: String) {
        currentSearch = query
        val curr = _uiState.value as? AdminNotesUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadNotes(showLoading = false)
    }

    fun moderateNote(noteId: String, action: String, reason: String? = null) {
        viewModelScope.launch {
            repository.moderateNote(noteId, action, reason)
                .onSuccess {
                    val message = when (action) {
                        "APPROVE" -> "Note approved and published successfully."
                        "REJECT" -> "Note rejected with reason recorded."
                        "REMOVE" -> "Note removed from public catalog."
                        "RESTORE" -> "Note restored and published."
                        else -> "Note moderation action completed."
                    }
                    val statusParam = if (currentStatus == "ALL") null else currentStatus
                    val searchParam = currentSearch.ifBlank { null }
                    repository.getAdminNotes(status = statusParam, search = searchParam)
                        .onSuccess { list ->
                            _uiState.value = AdminNotesUiState.Success(
                                notes = list,
                                selectedStatus = currentStatus,
                                searchQuery = currentSearch,
                                feedbackMessage = message
                            )
                        }
                }
                .onFailure { err ->
                    val curr = _uiState.value as? AdminNotesUiState.Success
                    if (curr != null) {
                        _uiState.value = curr.copy(feedbackMessage = "Moderation failed: ${err.message}")
                    }
                }
        }
    }

    fun deletePermanently(noteId: String) {
        viewModelScope.launch {
            repository.deleteNotePermanently(noteId)
                .onSuccess {
                    val statusParam = if (currentStatus == "ALL") null else currentStatus
                    val searchParam = currentSearch.ifBlank { null }
                    repository.getAdminNotes(status = statusParam, search = searchParam)
                        .onSuccess { list ->
                            _uiState.value = AdminNotesUiState.Success(
                                notes = list,
                                selectedStatus = currentStatus,
                                searchQuery = currentSearch,
                                feedbackMessage = "Note permanently deleted from database."
                            )
                        }
                }
                .onFailure { err ->
                    val curr = _uiState.value as? AdminNotesUiState.Success
                    if (curr != null) {
                        _uiState.value = curr.copy(feedbackMessage = "Failed to permanently delete note: ${err.message}")
                    }
                }
        }
    }

    fun clearFeedbackMessage() {
        val curr = _uiState.value as? AdminNotesUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(feedbackMessage = null)
        }
    }
}
