package com.campusverse.app.ui.screens.student.events

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.CampusEvent
import com.campusverse.app.ui.components.CampusVerseButton
import com.campusverse.app.ui.components.CampusVerseTopBar

@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedEventForDetails by remember { mutableStateOf<CampusEvent?>(null) }
    var selectedFilterChip by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Academic", "Technical", "Clubs", "Workshops", "Sports", "Cultural")

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is EventsUiState.Success && state.toastMessage != null) {
            snackbarHostState.showSnackbar(state.toastMessage)
            viewModel.clearToast()
        }
    }

    if (selectedEventForDetails != null) {
        AlertDialog(
            onDismissRequest = { selectedEventForDetails = null },
            title = { Text(selectedEventForDetails!!.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(selectedEventForDetails!!.description)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category: ${selectedEventForDetails!!.category}", fontWeight = FontWeight.SemiBold)
                    Text("Location: ${selectedEventForDetails!!.location}")
                    Text("Capacity: ${selectedEventForDetails!!.registeredCount}/${selectedEventForDetails!!.capacity} registered")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val event = selectedEventForDetails!!
                        viewModel.toggleRegistration(event)
                        selectedEventForDetails = null
                    }
                ) {
                    Text(if (selectedEventForDetails!!.isRegistered) "Cancel Registration" else "Register Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEventForDetails = null }) { Text("Close") }
            }
        )
    }

    Scaffold(
        topBar = {
            CampusVerseTopBar(
                title = "Events",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            com.campusverse.app.ui.screens.student.components.StudentBottomBar(
                currentRoute = com.campusverse.app.navigation.Screen.StudentHome.route,
                onNavigate = { route -> onNavigateToRoute?.invoke(route) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val state = uiState) {
            is EventsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is EventsUiState.Error -> {
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
                        CampusVerseButton(text = "Retry", onClick = { viewModel.loadEvents() })
                    }
                }
            }
            is EventsUiState.Success -> {
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
                            onValueChange = { viewModel.searchEvents(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search hackathons, workshops, webinars...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                Icon(imageVector = Icons.Outlined.FilterList, contentDescription = "Filter")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 2. Filter Chips Row (Stitch Spec)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterOptions.forEach { cat ->
                                FilterChip(
                                    selected = selectedFilterChip == cat,
                                    onClick = {
                                        selectedFilterChip = cat
                                        if (cat == "All") viewModel.setCategory("ALL")
                                        else viewModel.setCategory(cat.uppercase())
                                    },
                                    label = { Text(cat) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }

                    // 3. Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Upcoming Events (${state.events.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // 4. Events List
                    if (state.events.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "No campus events match your selected filter.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(state.events) { event ->
                            EventCardItem(
                                event = event,
                                onToggleRegistration = { viewModel.toggleRegistration(event) },
                                onCardClick = { selectedEventForDetails = event }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCardItem(
    event: CampusEvent,
    onToggleRegistration: () -> Unit,
    onCardClick: () -> Unit
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
                        text = event.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (event.isRegistered) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Registered",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upcoming • 06:00 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${event.location} • ${event.registeredCount}/${event.capacity} enrolled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onCardClick, shape = RoundedCornerShape(10.dp)) {
                    Text("Details")
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (event.isRegistered) {
                    OutlinedButton(
                        onClick = onToggleRegistration,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel")
                    }
                } else {
                    Button(
                        onClick = onToggleRegistration,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}
