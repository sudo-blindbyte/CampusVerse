package com.campusverse.app.ui.screens.admin.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.AdminUserItem
import com.campusverse.app.navigation.Screen
import com.campusverse.app.ui.screens.admin.components.AdminBottomBar
import com.campusverse.app.ui.theme.AdminTheme

/**
 * ADM — User Management Screen
 * Complete CRUD: Directory, status controls, user creation, deletion, role reassignments, and password resets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: AdminUserManagementViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateUserDialog by remember { mutableStateOf(false) }

    AdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "User Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadUsers() }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateUserDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Create User")
                }
            },
            bottomBar = {
                AdminBottomBar(
                    currentRoute = Screen.AdminUserManagement.route,
                    onNavigateToDashboard = onNavigateToDashboard,
                    onNavigateToUsers = {},
                    onNavigateToReports = onNavigateToReports,
                    onNavigateToSettings = onNavigateToSettings
                )
            },
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            when (val state = uiState) {
                is AdminUserManagementUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                is AdminUserManagementUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadUsers() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is AdminUserManagementUiState.Success -> {
                    UserManagementContent(
                        state = state,
                        paddingValues = innerPadding,
                        onSearch = { viewModel.onSearchQueryChanged(it) },
                        onRoleFilter = { viewModel.onRoleSelected(it) },
                        onSelectUser = { viewModel.onSelectUserForDetail(it) },
                        onToggleSuspend = { viewModel.toggleUserSuspension(it) },
                        onChangeRole = { u, r -> viewModel.changeUserRole(u, r) },
                        onResetPassword = { viewModel.resetPassword(it) },
                        onDeleteUser = { viewModel.deleteUser(it) }
                    )
                }
            }

            if (showCreateUserDialog) {
                CreateUserDialog(
                    onDismiss = { showCreateUserDialog = false },
                    onCreate = { name, email, pass, role ->
                        viewModel.createUser(name, email, pass, role) {
                            showCreateUserDialog = false
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserManagementContent(
    state: AdminUserManagementUiState.Success,
    paddingValues: PaddingValues,
    onSearch: (String) -> Unit,
    onRoleFilter: (String?) -> Unit,
    onSelectUser: (AdminUserItem?) -> Unit,
    onToggleSuspend: (AdminUserItem) -> Unit,
    onChangeRole: (String, String) -> Unit,
    onResetPassword: (String) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var showRoleDialogForUser by remember { mutableStateOf<AdminUserItem?>(null) }
    var showSuspendConfirmForUser by remember { mutableStateOf<AdminUserItem?>(null) }
    var showDeleteConfirmForUser by remember { mutableStateOf<AdminUserItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Feedback toast / alert banner if present
        if (!state.actionFeedback.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = state.actionFeedback,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            placeholder = { Text("Search users by name or email...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        // Role Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val roles = listOf("ALL", "STUDENT", "ALUMNI", "ASPIRANT", "ADMIN")
            roles.forEach { role ->
                val selected = (state.selectedRole == null && role == "ALL") || state.selectedRole == role
                FilterChip(
                    selected = selected,
                    onClick = { onRoleFilter(if (role == "ALL") null else role) },
                    label = { Text(role.replace("_", " ")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No users found matching query.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.users) { user ->
                    UserCard(
                        user = user,
                        onClick = { onSelectUser(user) },
                        onSuspendClick = { showSuspendConfirmForUser = user },
                        onChangeRoleClick = { showRoleDialogForUser = user }
                    )
                }
            }
        }
    }

    // Detail Modal Dialog
    if (state.selectedUserDetail != null) {
        val u = state.selectedUserDetail
        AlertDialog(
            onDismissRequest = { onSelectUser(null) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = u.fullName, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(text = "Email: ${u.email}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Role: ${u.role}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "Status: ${if (u.isActive) "Active" else "Suspended"}", color = if (u.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    if (!u.headline.isNullOrBlank()) Text(text = "Headline: ${u.headline}", style = MaterialTheme.typography.bodySmall)
                    if (!u.location.isNullOrBlank()) Text(text = "Location: ${u.location}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onResetPassword(u.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset User Password")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showDeleteConfirmForUser = u },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete User Account")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onSelectUser(null) }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmForUser != null) {
        val target = showDeleteConfirmForUser!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmForUser = null },
            title = { Text("Delete User Permanently?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete '${target.fullName}' (${target.email})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target.id)
                        showDeleteConfirmForUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmForUser = null }) { Text("Cancel") }
            }
        )
    }

    // Suspend / Reactivate Confirmation Dialog
    if (showSuspendConfirmForUser != null) {
        val target = showSuspendConfirmForUser!!
        AlertDialog(
            onDismissRequest = { showSuspendConfirmForUser = null },
            title = {
                Text(
                    text = if (target.isActive) "Suspend User Account?" else "Reactivate Account?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (target.isActive)
                        "Are you sure you want to suspend '${target.fullName}' (${target.email})? They will lose access to campus tools until reactivated."
                    else
                        "Reactivate account access for '${target.fullName}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleSuspend(target)
                        showSuspendConfirmForUser = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(if (target.isActive) "Confirm Suspension" else "Reactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendConfirmForUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Role Reassignment Dialog
    if (showRoleDialogForUser != null) {
        val target = showRoleDialogForUser!!
        AlertDialog(
            onDismissRequest = { showRoleDialogForUser = null },
            title = { Text(text = "Change Role for ${target.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val candidateRoles = listOf("STUDENT", "ALUMNI", "ASPIRANT", "ADMIN")
                    candidateRoles.forEach { candidate ->
                        OutlinedButton(
                            onClick = {
                                onChangeRole(target.id, candidate)
                                showRoleDialogForUser = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Set to $candidate")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRoleDialogForUser = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STUDENT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New User", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("User Role", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("STUDENT", "ALUMNI", "ASPIRANT", "ADMIN").forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r.take(3)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, email, password, role) },
                enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Create User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun UserCard(
    user: AdminUserItem,
    onClick: () -> Unit,
    onSuspendClick: () -> Unit,
    onChangeRoleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.fullName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (user.role) {
                        "ADMIN" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        "ALUMNI" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        "ASPIRANT" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = user.role,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (user.role) {
                            "ADMIN" -> MaterialTheme.colorScheme.secondary
                            "ALUMNI" -> MaterialTheme.colorScheme.tertiary
                            "ASPIRANT" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (user.isActive) Icons.Filled.CheckCircle else Icons.Filled.Block,
                        contentDescription = null,
                        tint = if (user.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isActive) "Active Account" else "Suspended",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onChangeRoleClick) {
                        Text("Role", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    TextButton(onClick = onSuspendClick) {
                        Text(
                            text = if (user.isActive) "Suspend" else "Activate",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (user.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
