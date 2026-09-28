package com.campusverse.app.ui.screens.student.notes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.NoteItem
import com.campusverse.app.data.model.StudentNotesPreferences
import com.campusverse.app.data.preferences.ModulePreferencesManager
import com.campusverse.app.data.repository.NetworkStudentRepository
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.domain.student.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StudentNotesTab {
    ALL,
    MY_NOTES
}

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data object PreScreenRequired : NotesUiState
    data class Success(
        val notes: List<NoteItem>,
        val preferences: StudentNotesPreferences? = null,
        val searchQuery: String = "",
        val selectedTag: String? = null,
        val selectedTab: StudentNotesTab = StudentNotesTab.ALL,
        val actionMessage: String? = null
    ) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

class NotesViewModel(
    private val repository: StudentRepository = NetworkStudentRepository.instance,
    private var prefsManager: ModulePreferencesManager? = ModulePreferencesManager.getInstanceOrNull()
) : ViewModel() {

    constructor(application: Application) : this(
        repository = NetworkStudentRepository.instance,
        prefsManager = ModulePreferencesManager.getInstance(application)
    )

    fun setPreferencesManager(manager: ModulePreferencesManager) {
        this.prefsManager = manager
    }

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var currentTab: StudentNotesTab = StudentNotesTab.ALL
    private var currentUserId: String = "default_student"

    init {
        if (prefsManager == null) {
            loadNotes()
        }
    }

    fun checkAndLoadNotes(currentUser: AuthenticatedUser?, forcePreferencesEdit: Boolean = false) {
        viewModelScope.launch {
            val userId = currentUser?.userId ?: "default_student"
            currentUserId = userId
            val isSetupDone = prefsManager?.isModuleSetupCompleted(userId, "student_notes") ?: false

            if (!isSetupDone || forcePreferencesEdit) {
                _uiState.value = NotesUiState.PreScreenRequired
                return@launch
            }

            loadNotesInternal(userId)
        }
    }

    fun onPreScreenCompleted(currentUser: AuthenticatedUser?) {
        val userId = currentUser?.userId ?: "default_student"
        currentUserId = userId
        viewModelScope.launch {
            loadNotesInternal(userId)
        }
    }

    fun editPreferences() {
        _uiState.value = NotesUiState.PreScreenRequired
    }

    fun selectTab(tab: StudentNotesTab) {
        currentTab = tab
        viewModelScope.launch {
            loadNotesInternal(currentUserId)
        }
    }

    fun loadNotes(search: String? = null, tag: String? = null) {
        viewModelScope.launch {
            loadNotesInternal(currentUserId, search, tag)
        }
    }

    private suspend fun loadNotesInternal(userId: String, search: String? = null, tag: String? = null, showLoading: Boolean = true) {
        if (showLoading && _uiState.value !is NotesUiState.Success) {
            _uiState.value = NotesUiState.Loading
        }
        try {
            val prefsJson = prefsManager?.getModulePreferences(userId, "student_notes")
            val prefs = prefsJson?.let { StudentNotesPreferences.fromJson(it) }

            val list = if (currentTab == StudentNotesTab.MY_NOTES) {
                val result = repository.getMyNotes()
                result.getOrDefault(emptyList())
            } else {
                val result = repository.getNotes(search = search, tag = tag)
                result.getOrDefault(emptyList())
            }

            // Rank notes: prioritizing those matching user's priority subjects (for All tab)
            val rankedList = if (currentTab == StudentNotesTab.ALL && prefs != null && search.isNullOrBlank() && tag.isNullOrBlank()) {
                list.sortedByDescending { note ->
                    val matchesSubject = prefs.prioritySubjects.any { subj ->
                        note.title.contains(subj, ignoreCase = true) ||
                        (note.description?.contains(subj, ignoreCase = true) == true) ||
                        (note.tags.any { t -> t.contains(subj, ignoreCase = true) })
                    }
                    if (matchesSubject) 1 else 0
                }
            } else {
                list
            }

            _uiState.value = NotesUiState.Success(
                notes = rankedList,
                preferences = prefs,
                searchQuery = search ?: "",
                selectedTag = tag,
                selectedTab = currentTab
            )
        } catch (e: Exception) {
            if (_uiState.value !is NotesUiState.Success) {
                _uiState.value = NotesUiState.Error(e.message ?: "Failed to load notes.")
            }
        }
    }

    fun searchNotes(query: String) {
        val curr = _uiState.value as? NotesUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        viewModelScope.launch {
            loadNotesInternal(currentUserId, search = query.ifBlank { null }, showLoading = false)
        }
    }

    fun createNote(
        title: String,
        description: String?,
        fileUrl: String,
        tags: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.createNote(
                    title = title,
                    description = description,
                    fileUrl = fileUrl.ifBlank { "https://docs.campusverse.edu/notes/upload_sample.pdf" },
                    courseId = null,
                    tags = tags
                )
                if (result.isSuccess) {
                    currentTab = StudentNotesTab.MY_NOTES
                    loadNotesInternal(currentUserId)
                    val curr = _uiState.value as? NotesUiState.Success
                    if (curr != null) {
                        _uiState.value = curr.copy(actionMessage = "Note uploaded successfully! Submitted for admin review.")
                    }
                    onSuccess()
                }
            } catch (_: Exception) {}
        }
    }

    fun requestRemoval(noteId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.requestNoteRemoval(noteId, reason)
            loadNotesInternal(currentUserId)
            val curr = _uiState.value as? NotesUiState.Success
            if (curr != null) {
                if (result.isSuccess) {
                    _uiState.value = curr.copy(actionMessage = "Removal request submitted for moderation review.")
                } else {
                    _uiState.value = curr.copy(actionMessage = "Failed to submit removal request: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun reportNote(noteId: String, reason: String) {
        viewModelScope.launch {
            repository.reportNote(noteId, reason)
            val curr = _uiState.value as? NotesUiState.Success
            if (curr != null) {
                _uiState.value = curr.copy(actionMessage = "Note reported to campus moderation.")
            }
        }
    }

    fun clearActionMessage() {
        val curr = _uiState.value as? NotesUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(actionMessage = null)
        }
    }
}

