package com.campusverse.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.UserRole
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.ui.components.CampusVerseErrorMessage
import com.campusverse.app.ui.components.CampusVersePrimaryButton
import com.campusverse.app.ui.components.CampusVerseTextField
import com.campusverse.app.ui.components.CampusVerseTopBar

/**
 * ADM — Admin Login Screen
 * Styled consistently with Student / Aspirant / Alumni login screens.
 */
@Composable
fun AdminLoginScreen(
    currentUser: AuthenticatedUser?,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdminLoginViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Auto-redirect if already logged in as authorized admin
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentUser.role == UserRole.ADMIN && currentUser.isAdminAuthorized) {
            onNavigateToDashboard()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AdminLoginUiState.Success) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            CampusVerseTopBar(
                title = "",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Admin Portal",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in to access the CampusVerse Admin Console",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                val formState = uiState as? AdminLoginUiState.Form

                if (formState?.errorMessage != null) {
                    CampusVerseErrorMessage(message = formState.errorMessage)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Email field
                CampusVerseTextField(
                    value = formState?.email ?: "",
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Admin Email",
                    placeholder = "admin@campusverse.edu",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                CampusVerseTextField(
                    value = formState?.password ?: "",
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = "Password",
                    placeholder = "Enter your admin password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.login()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (formState?.isLoading == true) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    CampusVersePrimaryButton(
                        text = "Sign In as Admin",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary navigation back to standard login
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign in as Student, Alumni, or Aspirant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
