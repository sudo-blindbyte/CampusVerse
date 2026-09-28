package com.campusverse.app.ui.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.AdminUserItem
import com.campusverse.app.data.repository.NetworkAdminRepository
import com.campusverse.app.domain.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUserManagementUiState {
    object Loading : AdminUserManagementUiState
    data class Success(
        val users: List<AdminUserItem>,
        val searchQuery: String = "",
        val selectedRole: String? = null,
        val selectedStatus: String? = null,
        val selectedUserDetail: AdminUserItem? = null,
        val actionFeedback: String? = null
    ) : AdminUserManagementUiState
    data class Error(val message: String) : AdminUserManagementUiState
}

class AdminUserManagementViewModel(
    private val repository: AdminRepository = NetworkAdminRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUserManagementUiState>(AdminUserManagementUiState.Loading)
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    private var currentSearch: String = ""
    private var currentRole: String? = null
    private var currentStatus: String? = null

    init {
        loadUsers()
    }

    fun loadUsers(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is AdminUserManagementUiState.Success) {
                _uiState.value = AdminUserManagementUiState.Loading
            }
            repository.getUsers(search = currentSearch, role = currentRole, status = currentStatus)
                .onSuccess { list ->
                    _uiState.value = AdminUserManagementUiState.Success(
                        users = list,
                        searchQuery = currentSearch,
                        selectedRole = currentRole,
                        selectedStatus = currentStatus
                    )
                }
                .onFailure { err ->
                    if (_uiState.value !is AdminUserManagementUiState.Success) {
                        _uiState.value = AdminUserManagementUiState.Error(err.message ?: "Failed to load users")
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        val curr = _uiState.value as? AdminUserManagementUiState.Success
        if (curr != null) {
            _uiState.value = curr.copy(searchQuery = query)
        }
        loadUsers(showLoading = false)
    }

    fun onRoleSelected(role: String?) {
        currentRole = role
        loadUsers()
    }

    fun onStatusSelected(status: String?) {
        currentStatus = status
        loadUsers()
    }

    fun onSelectUserForDetail(user: AdminUserItem?) {
        val current = _uiState.value
        if (current is AdminUserManagementUiState.Success) {
            _uiState.value = current.copy(selectedUserDetail = user, actionFeedback = null)
        }
    }

    fun toggleUserSuspension(user: AdminUserItem) {
        viewModelScope.launch {
            val newActive = !user.isActive
            repository.updateUserStatus(
                userId = user.id,
                isActive = newActive,
                reason = if (!newActive) "Suspended by Administrator" else "Reactivated by Administrator"
            ).onSuccess { updated ->
                val current = _uiState.value
                if (current is AdminUserManagementUiState.Success) {
                    val updatedList = current.users.map { if (it.id == user.id) it.copy(isActive = newActive) else it }
                    _uiState.value = current.copy(
                        users = updatedList,
                        selectedUserDetail = current.selectedUserDetail?.copy(isActive = newActive),
                        actionFeedback = if (newActive) "User account reactivated." else "User account suspended."
                    )
                }
            }
        }
    }

    fun changeUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId = userId, role = newRole)
                .onSuccess {
                    val current = _uiState.value
                    if (current is AdminUserManagementUiState.Success) {
                        val updatedList = current.users.map { if (it.id == userId) it.copy(role = newRole) else it }
                        _uiState.value = current.copy(
                            users = updatedList,
                            selectedUserDetail = current.selectedUserDetail?.copy(role = newRole),
                            actionFeedback = "Role updated to $newRole"
                        )
                    }
                }
        }
    }

    fun resetPassword(userId: String) {
        viewModelScope.launch {
            repository.resetUserPassword(userId)
                .onSuccess { msgOrPass ->
                    val current = _uiState.value
                    if (current is AdminUserManagementUiState.Success) {
                        val feedback = if (msgOrPass.contains("TempPass")) {
                            "Password reset! Temporary password: $msgOrPass"
                        } else {
                            "Password reset instructions dispatched to user's email."
                        }
                        _uiState.value = current.copy(
                            actionFeedback = feedback
                        )
                    }
                }
        }
    }

    fun createUser(name: String, email: String, password: String, role: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return
        viewModelScope.launch {
            repository.createUser(name, email, password, role)
                .onSuccess { newUser ->
                    loadUsers(showLoading = false)
                    val current = _uiState.value
                    if (current is AdminUserManagementUiState.Success) {
                        _uiState.value = current.copy(
                            actionFeedback = "User ${newUser.fullName} created successfully."
                        )
                    }
                    onSuccess()
                }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
                .onSuccess {
                    val current = _uiState.value
                    if (current is AdminUserManagementUiState.Success) {
                        val updatedList = current.users.filterNot { it.id == userId }
                        _uiState.value = current.copy(
                            users = updatedList,
                            selectedUserDetail = null,
                            actionFeedback = "User account permanently deleted."
                        )
                    }
                }
        }
    }
}
