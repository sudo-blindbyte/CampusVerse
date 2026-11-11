package com.campusverse.app.ui.screens.student.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.NoteItem
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.ui.components.CampusVerseButton
import com.campusverse.app.ui.components.CampusVerseTopBar

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    currentUser: AuthenticatedUser? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCreateNote: () -> Unit = {},
    onNavigateToRoute: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedNoteForReport by remember { mutableStateOf<NoteItem?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var downloadedNoteMessage by remember { mutableStateOf<String?>(null) }
    var showCreateNoteSheet by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteDescription by remember { mutableStateOf("") }
    var newNoteTags by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("All") }
    var bookmarkedNoteIds by remember { mutableStateOf(setOf<String>()) }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(currentUser) {
        viewModel.setPreferencesManager(com.campusverse.app.data.preferences.ModulePreferencesManager.getInstance(context))
        viewModel.checkAndLoadNotes(currentUser)
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is NotesUiState.Success && state.actionMessage != null) {
            snackbarHostState.showSnackbar(state.actionMessage)
            viewModel.clearActionMessage()
        }
    }

    if (downloadedNoteMessage != null) {
        AlertDialog(
            onDismissRequest = { downloadedNoteMessage = null },
            title = { Text("Study Resource") },
            text = { Text(downloadedNoteMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { downloadedNoteMessage = null }) { Text("OK") }
            }
        )
    }

    if (selectedNoteForReport != null) {
        AlertDialog(
            onDismissRequest = { selectedNoteForReport = null },
            title = { Text("Report Study Note") },
            text = {
                Column {
                    Text("Please explain why this note violates campus academic guidelines:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("e.g. Inappropriate content, copyright...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reportReason.isNotBlank() && selectedNoteForReport != null) {
                            viewModel.reportNote(selectedNoteForReport!!.id, reportReason)
                            selectedNoteForReport = null
                            reportReason = ""
                        }
                    }
                ) { Text("Submit Report") }
            },
            dismissButton = {
                TextButton(onClick = { selectedNoteForReport = null }) { Text("Cancel") }
            }
        )
    }

    var selectedNoteForRemoval by remember { mutableStateOf<NoteItem?>(null) }
    var removalReason by remember { mutableStateOf("") }

    if (selectedNoteForRemoval != null) {
        AlertDialog(
            onDismissRequest = {
                selectedNoteForRemoval = null
                removalReason = ""
            },
            title = { Text("Request Note Removal") },
            text = {
                Column {
                    Text(
                        text = "To maintain academic integrity, direct deletion is disabled. Please explain why '${selectedNoteForRemoval?.title}' should be removed. A moderator will review your request:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = removalReason,
                        onValueChange = { removalReason = it },
                        placeholder = { Text("Reason for removal (e.g. outdated syllabus, revised version)...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (removalReason.isNotBlank() && selectedNoteForRemoval != null) {
                            viewModel.requestRemoval(selectedNoteForRemoval!!.id, removalReason.trim())
                            selectedNoteForRemoval = null
                            removalReason = ""
                        }
                    },
                    enabled = removalReason.isNotBlank()
                ) { Text("Submit Request") }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedNoteForRemoval = null
                    removalReason = ""
                }) { Text("Cancel") }
            }
        )
    }


    when (val state = uiState) {
        is NotesUiState.PreScreenRequired -> {
            StudentNotesPreScreen(
                currentUser = currentUser,
                onCompleted = { viewModel.onPreScreenCompleted(currentUser) },
                onNavigateBack = onNavigateBack,
                onNavigateToRoute = onNavigateToRoute
            )
        }
        is NotesUiState.Loading -> {
            Scaffold(
                topBar = {
                    CampusVerseTopBar(
                        title = "Notes Hub",
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        is NotesUiState.Error -> {
            Scaffold(
                topBar = {
                    CampusVerseTopBar(
                        title = "Notes Hub",
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
                        CampusVerseButton(text = "Retry", onClick = { viewModel.checkAndLoadNotes(currentUser) })
                    }
                }
            }
        }
        is NotesUiState.Success -> {
            val prefs = state.preferences
            val dynamicFilterOptions = remember(prefs) {
                val list = mutableListOf("All")
                prefs?.prioritySubjects?.let { list.addAll(it) }
                list.addAll(listOf("Verified Notes", "Trending"))
                list.distinct()
            }

            Scaffold(
                topBar = {
                    CampusVerseTopBar(
                        title = "Notes Hub",
                        canNavigateBack = true,
                        onNavigateBack = onNavigateBack,
                        actions = {
                            IconButton(onClick = { viewModel.editPreferences() }) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "Preferences",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    com.campusverse.app.ui.screens.student.components.StudentBottomBar(
                        currentRoute = com.campusverse.app.navigation.Screen.Academics.route,
                        onNavigate = { route -> onNavigateToRoute?.invoke(route) }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (onNavigateToCreateNote != {}) {
                                onNavigateToCreateNote()
                            }
                            showCreateNoteSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create Note",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabRow(
                        selectedTabIndex = if (state.selectedTab == StudentNotesTab.ALL) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = state.selectedTab == StudentNotesTab.ALL,
                            onClick = { viewModel.selectTab(StudentNotesTab.ALL) },
                            text = { Text("All Notes") }
                        )
                        Tab(
                            selected = state.selectedTab == StudentNotesTab.MY_NOTES,
                            onClick = { viewModel.selectTab(StudentNotesTab.MY_NOTES) },
                            text = { Text("My Notes") }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Personalized Study Preferences Banner (Only in All Notes)
                        if (state.selectedTab == StudentNotesTab.ALL && prefs != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Study Priorities",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            TextButton(
                                                onClick = { viewModel.editPreferences() },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = "Edit Preferences",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text(
                                            text = prefs.studyingField,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            prefs.prioritySubjects.forEach { subj ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface
                                                ) {
                                                    Text(
                                                        text = "★ $subj",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "⏱ ${prefs.dailyStudyTime}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Search Bar
                        item {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.searchNotes(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        if (state.selectedTab == StudentNotesTab.MY_NOTES)
                                            "Filter your uploaded notes..."
                                        else
                                            "Search notes by title, topic, or tags..."
                                    )
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // 3. Filter Chips Row (Only in All Notes)
                        if (state.selectedTab == StudentNotesTab.ALL) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    dynamicFilterOptions.forEach { filter ->
                                        FilterChip(
                                            selected = selectedFilterChip == filter,
                                            onClick = {
                                                selectedFilterChip = filter
                                                if (filter == "All") viewModel.searchNotes("")
                                                else viewModel.searchNotes(filter)
                                            },
                                            label = { Text(filter) },
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Section Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (state.selectedTab == StudentNotesTab.MY_NOTES)
                                        "My Uploaded Notes (${state.notes.size})"
                                    else
                                        "Recommended Notes (${state.notes.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // 5. Empty State
                        if (state.notes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.selectedTab == StudentNotesTab.MY_NOTES)
                                            "You haven't uploaded any study notes yet. Tap '+' below to share your notes with the campus!"
                                        else
                                            "No study notes found.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 6. Notes List
                        items(state.notes, key = { it.id }) { note ->
                            val isBookmarked = bookmarkedNoteIds.contains(note.id)
                            val isOwner = note.isOwnedByCurrentUser || note.userId == (currentUser?.userId ?: "")
                            val canRequestRemoval = isOwner && note.status != "REMOVAL_REQUESTED" && note.status != "REMOVED"

                            NoteCardItem(
                                note = note,
                                isBookmarked = isBookmarked,
                                isMyNotesTab = state.selectedTab == StudentNotesTab.MY_NOTES,
                                onBookmarkToggle = {
                                    bookmarkedNoteIds = if (isBookmarked) {
                                        bookmarkedNoteIds - note.id
                                    } else {
                                        bookmarkedNoteIds + note.id
                                    }
                                },
                                onDownloadClick = {
                                    downloadedNoteMessage = "Downloaded '${note.title}' to local device."
                                },
                                onReportClick = {
                                    selectedNoteForReport = note
                                },
                                onRequestRemovalClick = if (canRequestRemoval) {
                                    { selectedNoteForRemoval = note }
                                } else null
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }

            if (showCreateNoteSheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showCreateNoteSheet = false },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Upload Study Note",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            label = { Text("Note Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newNoteDescription,
                            onValueChange = { newNoteDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newNoteTags,
                            onValueChange = { newNoteTags = it },
                            label = { Text("Tags (comma-separated, e.g. cse, exam)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CampusVerseButton(
                            text = "Upload Note",
                            onClick = {
                                if (newNoteTitle.isNotBlank()) {
                                    viewModel.createNote(
                                        title = newNoteTitle.trim(),
                                        description = newNoteDescription.trim().ifBlank { null },
                                        fileUrl = "https://docs.campusverse.edu/notes/upload_sample.pdf",
                                        tags = newNoteTags.trim().ifBlank { null },
                                        onSuccess = {
                                            showCreateNoteSheet = false
                                            newNoteTitle = ""
                                            newNoteDescription = ""
                                            newNoteTags = ""
                                        }
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteCardItem(
    note: NoteItem,
    isBookmarked: Boolean,
    isMyNotesTab: Boolean = false,
    onBookmarkToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    onReportClick: () -> Unit,
    onRequestRemovalClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (note.authorName.isNotBlank()) {
                        Text(
                            text = "Shared by ${note.authorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Badge (shown in My Notes or if not PUBLISHED)
            if (isMyNotesTab || note.status != "PUBLISHED") {
                Spacer(modifier = Modifier.height(8.dp))
                val (badgeText, badgeColor, badgeBg) = when (note.status) {
                    "PENDING_REVIEW" -> Triple("Pending Review", Color(0xFFE65100), Color(0xFFFFF3E0))
                    "PUBLISHED" -> Triple("Published", Color(0xFF2E7D32), Color(0xFFE8F5E9))
                    "REJECTED" -> Triple("Rejected", Color(0xFFC62828), Color(0xFFFFEBEE))
                    "REMOVAL_REQUESTED" -> Triple("Removal Requested", Color(0xFFE65100), Color(0xFFFFF8E1))
                    "REMOVED" -> Triple("Removed", Color(0xFF424242), Color(0xFFEEEEEE))
                    else -> Triple(note.status, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Rejection reason alert
            if (note.status == "REJECTED" && !note.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rejection Reason: ${note.rejectionReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Removal reason alert
            if (note.status == "REMOVAL_REQUESTED" && !note.removalReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF8E1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Removal Request Reason: ${note.removalReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (!note.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!note.tags.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDownloadClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", style = MaterialTheme.typography.labelMedium)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onRequestRemovalClick != null) {
                        OutlinedButton(
                            onClick = onRequestRemovalClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Request Removal", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (onRequestRemovalClick == null) {
                        IconButton(onClick = onReportClick) {
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = "Report Note",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

