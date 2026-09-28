package com.campusverse.app.ui.screens.alumni.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.ChatMessageItem
import com.campusverse.app.data.model.ConversationItem
import com.campusverse.app.data.repository.NetworkAlumniRepository
import com.campusverse.app.domain.alumni.AlumniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlumniConversationsUiState {
    data object Loading : AlumniConversationsUiState
    data class Success(val conversations: List<ConversationItem>, val searchQuery: String = "") : AlumniConversationsUiState
    data class Error(val message: String) : AlumniConversationsUiState
}

sealed interface AlumniChatUiState {
    data object Loading : AlumniChatUiState
    data class Success(
        val conversationId: String,
        val title: String,
        val messages: List<ChatMessageItem>
    ) : AlumniChatUiState
    data class Error(val message: String) : AlumniChatUiState
}

class AlumniMessagingViewModel(
    private val repository: AlumniRepository = NetworkAlumniRepository.instance
) : ViewModel() {

    private val _conversationsState = MutableStateFlow<AlumniConversationsUiState>(AlumniConversationsUiState.Loading)
    val conversationsState: StateFlow<AlumniConversationsUiState> = _conversationsState.asStateFlow()

    private val _chatState = MutableStateFlow<AlumniChatUiState>(AlumniChatUiState.Loading)
    val chatState: StateFlow<AlumniChatUiState> = _chatState.asStateFlow()

    private var activeConversationId: String = ""

    init {
        loadConversations()
    }

    fun loadConversations(search: String? = null, showLoading: Boolean = true) {
        val currState = _conversationsState.value as? AlumniConversationsUiState.Success
        if (currState != null && search != null) {
            _conversationsState.value = currState.copy(searchQuery = search)
        }
        viewModelScope.launch {
            if (showLoading && _conversationsState.value !is AlumniConversationsUiState.Success) {
                _conversationsState.value = AlumniConversationsUiState.Loading
            }
            repository.getConversations(search)
                .onSuccess { list ->
                    _conversationsState.value = AlumniConversationsUiState.Success(list, search ?: "")
                }
                .onFailure { err ->
                    if (_conversationsState.value !is AlumniConversationsUiState.Success) {
                        _conversationsState.value = AlumniConversationsUiState.Error(err.message ?: "Failed to load conversations.")
                    }
                }
        }
    }

    fun loadChat(conversationId: String) {
        activeConversationId = conversationId
        viewModelScope.launch {
            _chatState.value = AlumniChatUiState.Loading
            repository.getMessages(conversationId)
                .onSuccess { messages ->
                    _chatState.value = AlumniChatUiState.Success(conversationId, "Direct Chat", messages)
                }
                .onFailure { err ->
                    _chatState.value = AlumniChatUiState.Error(err.message ?: "Failed to load messages.")
                }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || activeConversationId.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(activeConversationId, content)
            loadChat(activeConversationId)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            loadChat(activeConversationId)
        }
    }

    fun reportUser(userId: String, reason: String) {
        viewModelScope.launch {
            repository.reportUser(userId, reason)
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId)
        }
    }
}
