package com.campusverse.app.ui.screens.aspirant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.AdmissionPredictionItem
import com.campusverse.app.data.model.AspirantHomeSummary
import com.campusverse.app.data.model.CollegeItem
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.navigation.Screen
import com.campusverse.app.ui.screens.aspirant.components.AspirantBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AspirantHomeScreen(
    currentUser: AuthenticatedUser?,
    onNavigateToCollegeExplorer: () -> Unit,
    onNavigateToCollegeDetails: (String) -> Unit = {},
    onNavigateToCollegeComparison: () -> Unit,
    onNavigateToSavedColleges: () -> Unit = {},
    onNavigateToPredictor: () -> Unit,
    onNavigateToScholarships: () -> Unit,
    onNavigateToSavedScholarships: () -> Unit,
    onNavigateToAiRecommendations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToStore: () -> Unit = {},
    viewModel: AspirantHomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCollegeId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadHomeSummary()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CampusVerse",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aspirant Portal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStore) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = "Aspirant Store"
                        )
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AspirantBottomBar(
                currentRoute = Screen.AspirantHome.route,
                onNavigate = { route ->
                    when (route) {
                        Screen.AspirantCollegeExplorer.route -> onNavigateToCollegeExplorer()
                        Screen.AspirantScholarships.route -> onNavigateToScholarships()
                        Screen.AspirantPredictor.route -> onNavigateToPredictor()
                        Screen.AspirantProfile.route -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is AspirantHomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is AspirantHomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadHomeSummary() }) {
                            Text("Retry")
                        }
                    }
                }
                is AspirantHomeUiState.Success -> {
                    AspirantHomeContent(
                        currentUser = currentUser,
                        summary = state.summary,
                        onNavigateToCollegeExplorer = onNavigateToCollegeExplorer,
                        onNavigateToCollegeDetails = { cId ->
                            if (onNavigateToCollegeDetails != {}) {
                                onNavigateToCollegeDetails(cId)
                            }
                            selectedCollegeId = cId
                        },
                        onNavigateToCollegeComparison = onNavigateToCollegeComparison,
                        onNavigateToSavedColleges = onNavigateToSavedColleges,
                        onNavigateToPredictor = onNavigateToPredictor,
                        onNavigateToScholarships = onNavigateToScholarships,
                        onNavigateToSavedScholarships = onNavigateToSavedScholarships,
                        onNavigateToAiRecommendations = onNavigateToAiRecommendations,
                        onToggleSave = { collegeId, saved -> viewModel.toggleSaveCollege(collegeId, saved) }
                    )
                }
            }
        }

        if (selectedCollegeId != null) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { selectedCollegeId = null },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                com.campusverse.app.ui.screens.aspirant.colleges.CollegeDetailScreen(
                    collegeId = selectedCollegeId!!,
                    onNavigateBack = { selectedCollegeId = null }
                )
            }
        }
    }
}

@Composable
private fun AspirantHomeContent(
    currentUser: com.campusverse.app.domain.auth.AuthenticatedUser? = null,
    summary: AspirantHomeSummary,
    onNavigateToCollegeExplorer: () -> Unit,
    onNavigateToCollegeDetails: (String) -> Unit = {},
    onNavigateToCollegeComparison: () -> Unit,
    onNavigateToSavedColleges: () -> Unit = {},
    onNavigateToPredictor: () -> Unit,
    onNavigateToScholarships: () -> Unit,
    onNavigateToSavedScholarships: () -> Unit,
    onNavigateToAiRecommendations: () -> Unit,
    onToggleSave: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayName = summary.profile.fullName.takeIf { it.isNotBlank() } ?: currentUser?.name ?: "Aspirant"
                        Column {
                            Text(
                                text = "Welcome, $displayName",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Target: ${summary.profile.targetDegree} • ${summary.profile.targetMajor}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metric Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadge(
                            label = "Saved Colleges",
                            count = summary.savedCollegesCount,
                            icon = Icons.Default.School,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f).clickable { onNavigateToSavedColleges() }
                        )
                        StatBadge(
                            label = "Scholarships",
                            count = summary.savedScholarshipsCount,
                            icon = Icons.Default.WorkspacePremium,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f).clickable { onNavigateToSavedScholarships() }
                        )
                        StatBadge(
                            label = "Predictions",
                            count = summary.recentPredictionsCount,
                            icon = Icons.Default.Calculate,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f).clickable { onNavigateToPredictor() }
                        )
                    }
                }
            }
        }

        // 2. Quick Hub Action Chips
        item {
            Column {
                Text(
                    text = "Admissions Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        QuickActionChip(
                            title = "AI Counselor",
                            icon = Icons.Default.AutoAwesome,
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = onNavigateToAiRecommendations
                        )
                    }
                    item {
                        QuickActionChip(
                            title = "Compare Colleges",
                            icon = Icons.Default.CompareArrows,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToCollegeComparison
                        )
                    }
                    item {
                        QuickActionChip(
                            title = "Predict Chances",
                            icon = Icons.Default.Calculate,
                            color = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToPredictor
                        )
                    }
                    item {
                        QuickActionChip(
                            title = "Find Scholarships",
                            icon = Icons.Default.WorkspacePremium,
                            color = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToScholarships
                        )
                    }
                }
            }
        }

        // 3. Recommended Colleges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended Colleges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToCollegeExplorer() }
                )
            }
        }

        items(summary.recommendedColleges) { college ->
            CollegeCard(
                college = college,
                onClick = { onNavigateToCollegeDetails(college.id) },
                onToggleSave = { onToggleSave(college.id, college.isSaved) }
            )
        }

        // 4. Recent Predictions Section
        if (summary.recentPredictions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Admission Predictions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(summary.recentPredictions) { pred ->
                PredictionSummaryCard(
                    prediction = pred,
                    onClick = onNavigateToPredictor
                )
            }
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun CollegeCard(
    college: CollegeItem,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
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
                        text = college.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${college.city ?: ""}, ${college.country}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onToggleSave) {
                    Icon(
                        imageVector = if (college.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (college.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Rank #${college.ranking}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${college.acceptanceRate}% Acceptance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = college.averageFees,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionSummaryCard(
    prediction: AdmissionPredictionItem,
    onClick: () -> Unit
) {
    val badgeColor = when (prediction.qualificationStatus) {
        "STRONG_CANDIDATE" -> MaterialTheme.colorScheme.tertiary
        "COMPETITIVE" -> MaterialTheme.colorScheme.primary
        "REACH" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prediction.institutionName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prediction.programName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${prediction.predictionPercentage}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                    Text(
                        text = prediction.qualificationStatus.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor
                    )
                }
            }
        }
    }
}
