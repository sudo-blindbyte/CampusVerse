package com.campusverse.app.ui.screens.student.library

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.LibraryResource
import com.campusverse.app.ui.components.CampusVerseButton
import com.campusverse.app.ui.components.CampusVerseTopBar

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var openedResource by remember { mutableStateOf<LibraryResource?>(null) }
    var selectedFilterChip by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Textbooks", "E-Books", "Reference", "Journals", "Past Papers")

    if (openedResource != null) {
        AlertDialog(
            onDismissRequest = { openedResource = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text(openedResource!!.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Author: ${openedResource!!.author}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Format: ${openedResource!!.category}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Location: ${openedResource!!.location ?: "IT Block — Library Floor 2"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status: Available in campus repository")
                }
            },
            confirmButton = {
                TextButton(onClick = { openedResource = null }) { Text("Open Resource") }
            },
            dismissButton = {
                TextButton(onClick = { openedResource = null }) { Text("Close") }
            }
        )
    }

    Scaffold(
        topBar = {
            CampusVerseTopBar(
                title = "Library",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            com.campusverse.app.ui.screens.student.components.StudentBottomBar(
                currentRoute = com.campusverse.app.navigation.Screen.Academics.route,
                onNavigate = { route -> onNavigateToRoute?.invoke(route) }
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is LibraryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        CampusVerseButton(text = "Retry", onClick = { viewModel.loadLibrary() })
                    }
                }
            }
            is LibraryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Search Bar
                    item {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.searchLibrary(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search title, author, or keyword...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                Icon(imageVector = Icons.Outlined.Tune, contentDescription = "Filter")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 2. Filter Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterOptions.forEach { filter ->
                                FilterChip(
                                    selected = selectedFilterChip == filter,
                                    onClick = {
                                        selectedFilterChip = filter
                                        if (filter == "All") viewModel.setCategory("ALL")
                                        else viewModel.setCategory(filter.uppercase())
                                    },
                                    label = { Text(filter) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }

                    // 3. Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recommended For You (${state.resources.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // 4. Resources List
                    if (state.resources.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "No library items matched your selection.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(state.resources) { item ->
                            LibraryCardItem(
                                resource = item,
                                onOpen = { openedResource = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryCardItem(
    resource: LibraryResource,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = resource.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "By ${resource.author} • ${resource.location ?: "IT / Operating Systems"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Available in Library",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "IT Block — Library Floor 2",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CampusVerseButton(
                    text = "View Details",
                    onClick = onOpen
                )
            }
        }
    }
}
