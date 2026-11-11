package com.campusverse.app.ui.screens.student.community

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.CommunityItem
import com.campusverse.app.ui.components.CampusVerseButton
import com.campusverse.app.ui.components.CampusVerseTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCommunityDetail: (String) -> Unit = {},
    onNavigateToRoute: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    var selectedFilterChip by remember { mutableStateOf("All") }
    var selectedCommunityId by remember { mutableStateOf<String?>(null) }
    var newPostTitle by remember { mutableStateOf("") }
    var newPostContent by remember { mutableStateOf("") }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "My Communities", "Academic", "Clubs", "Events", "Trending")

    Scaffold(
        topBar = {
            CampusVerseTopBar(
                title = "Community",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            com.campusverse.app.ui.screens.student.components.StudentBottomBar(
                currentRoute = com.campusverse.app.navigation.Screen.CommunityList.route,
                onNavigate = { route -> onNavigateToRoute?.invoke(route) }
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val state = listState) {
            is CommunityListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is CommunityListUiState.Error -> {
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
                        CampusVerseButton(text = "Retry", onClick = { viewModel.loadCommunities() })
                    }
                }
            }
            is CommunityListUiState.Success -> {
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
                            onValueChange = { viewModel.loadCommunities(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search developer clubs, study groups...") },
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
                                        if (filter == "All") viewModel.loadCommunities("")
                                        else viewModel.loadCommunities(filter)
                                    },
                                    label = { Text(filter) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }

                    // 3. Hero Community Banner (Stitch Spec)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CampusVerse Student Community",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Connect with classmates, join academic groups, share notes, and discover campus activities.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 4. Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recommended Communities (${state.communities.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // 5. Communities List
                    if (state.communities.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "No campus communities found matching your search.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(state.communities) { comm ->
                            CommunityCardItem(
                                community = comm,
                                onOpen = {
                                    onNavigateToCommunityDetail(comm.id)
                                },
                                onToggleJoin = { viewModel.toggleJoinCommunity(comm) }
                            )
                        }
                    }
                }
            }
        }

        // Community Details Sheet
        if (selectedCommunityId != null) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { selectedCommunityId = null },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                when (val dState = detailState) {
                    is CommunityDetailUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is CommunityDetailUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(text = dState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is CommunityDetailUiState.Success -> {
                        val comm = dState.community
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Column {
                                    Text(text = comm.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = comm.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "${comm.memberCount} Members • ${dState.posts.size} Posts", style = MaterialTheme.typography.labelMedium)
                                        CampusVerseButton(
                                            text = "New Post",
                                            onClick = { showCreatePostDialog = true },
                                            modifier = Modifier.width(130.dp)
                                        )
                                    }
                                }
                            }

                            if (showCreatePostDialog) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Create Discussion Post", fontWeight = FontWeight.Bold)
                                            OutlinedTextField(
                                                value = newPostTitle,
                                                onValueChange = { newPostTitle = it },
                                                label = { Text("Title") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = newPostContent,
                                                onValueChange = { newPostContent = it },
                                                label = { Text("Content") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                CampusVerseButton(
                                                    text = "Post",
                                                    onClick = {
                                                        if (newPostTitle.isNotBlank() && newPostContent.isNotBlank()) {
                                                            viewModel.createPost(comm.id, newPostTitle.trim(), newPostContent.trim()) {
                                                                showCreatePostDialog = false
                                                                newPostTitle = ""
                                                                newPostContent = ""
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedButton(
                                                    onClick = { showCreatePostDialog = false },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            items(dState.posts) { post ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(text = post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(text = post.authorName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(4.dp))
                                        Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { viewModel.likePost(post.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.ThumbUp,
                                                        contentDescription = "Like",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Text(text = "${post.likesCount}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityCardItem(
    community: CommunityItem,
    onOpen: () -> Unit,
    onToggleJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
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
                        text = "Academic Hub",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${community.memberCount}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = community.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = community.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${community.postsCount} active discussions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (community.isMember) {
                    OutlinedButton(
                        onClick = onToggleJoin,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Joined", color = MaterialTheme.colorScheme.tertiary)
                    }
                } else {
                    Button(
                        onClick = onToggleJoin,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Join")
                    }
                }
            }
        }
    }
}
