package com.campusverse.app.ui.screens.student.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.CommunityItem
import com.campusverse.app.data.model.CommunityPostItem
import com.campusverse.app.data.repository.NetworkStudentRepository
import com.campusverse.app.domain.student.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CommunityListUiState {
    data object Loading : CommunityListUiState
    data class Success(
        val communities: List<CommunityItem>,
        val searchQuery: String = ""
    ) : CommunityListUiState
    data class Error(val message: String) : CommunityListUiState
}

sealed interface CommunityDetailUiState {
    data object Loading : CommunityDetailUiState
    data class Success(
        val community: CommunityItem,
        val posts: List<CommunityPostItem>,
        val actionToast: String? = null
    ) : CommunityDetailUiState
    data class Error(val message: String) : CommunityDetailUiState
}

class CommunityViewModel(
    private val repository: StudentRepository = NetworkStudentRepository.instance
) : ViewModel() {

    private val _listState = MutableStateFlow<CommunityListUiState>(CommunityListUiState.Loading)
    val listState: StateFlow<CommunityListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<CommunityDetailUiState>(CommunityDetailUiState.Loading)
    val detailState: StateFlow<CommunityDetailUiState> = _detailState.asStateFlow()

    init {
        loadCommunities()
    }

    fun loadCommunities(search: String? = null, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _listState.value !is CommunityListUiState.Success) {
                _listState.value = CommunityListUiState.Loading
            }
            try {
                val result = repository.getCommunities(search = search)
                val list = result.getOrDefault(emptyList())
                _listState.value = CommunityListUiState.Success(
                    communities = list,
                    searchQuery = search ?: ""
                )
            } catch (e: Exception) {
                if (_listState.value !is CommunityListUiState.Success) {
                    _listState.value = CommunityListUiState.Error(e.message ?: "Failed to load communities.")
                }
            }
        }
    }

    fun loadCommunityDetails(communityId: String) {
        viewModelScope.launch {
            _detailState.value = CommunityDetailUiState.Loading
            try {
                val result = repository.getCommunityById(communityId)
                val pair = result.getOrNull()
                if (pair != null) {
                    _detailState.value = CommunityDetailUiState.Success(
                        community = pair.first,
                        posts = pair.second
                    )
                } else {
                    _detailState.value = CommunityDetailUiState.Error("Community not found.")
                }
            } catch (e: Exception) {
                _detailState.value = CommunityDetailUiState.Error(e.message ?: "Failed to load community details.")
            }
        }
    }

    fun toggleJoinCommunity(community: CommunityItem) {
        viewModelScope.launch {
            if (community.isMember) {
                repository.leaveCommunity(community.id)
            } else {
                repository.joinCommunity(community.id)
            }
            loadCommunities()
        }
    }

    fun createPost(communityId: String, title: String, content: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.createCommunityPost(communityId, title, content)
                loadCommunityDetails(communityId)
                onComplete()
            } catch (_: Exception) {}
        }
    }

    fun deletePost(communityId: String, postId: String) {
        viewModelScope.launch {
            repository.deleteCommunityPost(postId)
            loadCommunityDetails(communityId)
        }
    }

    fun likePost(postId: String) {
        val curr = _detailState.value as? CommunityDetailUiState.Success ?: return
        viewModelScope.launch {
            repository.likeCommunityPost(postId)
            val updated = curr.posts.map {
                if (it.id == postId) it.copy(likesCount = it.likesCount + 1) else it
            }
            _detailState.value = curr.copy(posts = updated)
        }
    }

    fun addComment(communityId: String, postId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.createComment(postId, content)
            loadCommunityDetails(communityId)
        }
    }

    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            repository.reportContent("POST", postId, reason)
            val curr = _detailState.value as? CommunityDetailUiState.Success
            if (curr != null) {
                _detailState.value = curr.copy(actionToast = "Report submitted for moderation.")
            }
        }
    }

    fun clearToast() {
        val curr = _detailState.value as? CommunityDetailUiState.Success ?: return
        _detailState.value = curr.copy(actionToast = null)
    }
}
