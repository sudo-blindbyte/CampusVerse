package com.campusverse.app.ui.screens.admin

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.AdminDashboardData
import com.campusverse.app.data.model.AuditLogItem
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.navigation.Screen
import com.campusverse.app.ui.screens.admin.components.AdminBottomBar

import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.AdminPanelSettings
import com.campusverse.app.ui.theme.AdminTheme

/**
 * ADM — Admin Dashboard Screen
 * Overview hub for campus administrators with live metrics, quick action tiles, and audit log.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: AuthenticatedUser?,
    onLogout: () -> Unit,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {},
    onNavigateToEventsJobs: () -> Unit = {},
    onNavigateToJobs: () -> Unit = {},
    onNavigateToMentorship: () -> Unit = {},
    onNavigateToAnnouncements: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    viewModel: AdminDashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CampusVerse",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Admin Control Center",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Admin Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.loadDashboard() }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentRoute = Screen.AdminDashboard.route,
                    onNavigateToDashboard = {},
                    onNavigateToUsers = onNavigateToUsers,
                    onNavigateToReports = onNavigateToReports,
                    onNavigateToSettings = onNavigateToSettings
                )
            },
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
        when (val state = uiState) {
            is AdminDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }
            is AdminDashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadDashboard() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                            Text("Retry")
                        }
                    }
                }
            }
            is AdminDashboardUiState.Success -> {
                DashboardContent(
                    data = state.data,
                    paddingValues = innerPadding,
                    onNavigateToFinance = onNavigateToFinance,
                    onNavigateToUsers = onNavigateToUsers,
                    onNavigateToVerification = onNavigateToVerification,
                    onNavigateToReports = onNavigateToReports,
                    onNavigateToMarketplace = onNavigateToMarketplace,
                    onNavigateToEvents = onNavigateToEventsJobs,
                    onNavigateToJobs = onNavigateToJobs,
                    onNavigateToMentorship = onNavigateToMentorship,
                    onNavigateToAnnouncements = onNavigateToAnnouncements,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToNotes = onNavigateToNotes
                )
            }
        }
    }
    }
}

@Composable
private fun DashboardContent(
    data: AdminDashboardData,
    paddingValues: PaddingValues,
    onNavigateToFinance: () -> Unit = {},
    onNavigateToUsers: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToMentorship: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotes: () -> Unit = {}
) {
    val m = data.metrics

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Metric Overview Cards
        item {
            Text(
                text = "Platform Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    title = "Total Users",
                    value = "${m.totalUsers}",
                    subtitle = "${m.studentsCount} Stud • ${m.alumniCount} Alum",
                    icon = Icons.Filled.People,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToUsers
                )
                AdminStatCard(
                    title = "Verifications",
                    value = "${m.pendingVerifications}",
                    subtitle = "Pending Review",
                    icon = Icons.Filled.VerifiedUser,
                    color = if (m.pendingVerifications > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToVerification
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    title = "Open Reports",
                    value = "${m.pendingReports}",
                    subtitle = "Flagged Content",
                    icon = Icons.Filled.Flag,
                    color = if (m.pendingReports > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
                AdminStatCard(
                    title = "Active Ops",
                    value = "${m.activeJobs + m.totalEvents}",
                    subtitle = "${m.activeJobs} Jobs • ${m.totalEvents} Events",
                    icon = Icons.Filled.EventNote,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToEvents
                )
            }
        }

        // 2. Action Tiles
        item {
            Text(
                text = "Administration Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminActionTile(
                    title = "Platform Finance & Revenue",
                    description = "Monitor platform revenue, inspect transactions, and execute customer refunds",
                    icon = Icons.Filled.CurrencyRupee,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToFinance
                )
                AdminActionTile(
                    title = "Verification Queue",
                    description = "${m.pendingVerifications} institutional records awaiting review",
                    icon = Icons.Filled.VerifiedUser,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToVerification
                )
                AdminActionTile(
                    title = "Marketplace Moderation",
                    description = "${m.activeMarketplaceListings} listings live on student exchange",
                    icon = Icons.Filled.ShoppingBag,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToMarketplace
                )
                AdminActionTile(
                    title = "Notes Moderation",
                    description = "Review and moderate student-uploaded study notes & requests",
                    icon = Icons.Filled.MenuBook,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToNotes
                )
                AdminActionTile(
                    title = "Campus Events Oversight",
                    description = "${m.totalEvents} institutional workshops & hackathons scheduled",
                    icon = Icons.Filled.EventNote,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToEvents
                )
                AdminActionTile(
                    title = "Job Postings Oversight",
                    description = "${m.activeJobs} active career opportunities & internships",
                    icon = Icons.Filled.Work,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToJobs
                )
                AdminActionTile(
                    title = "Mentorship Oversight",
                    description = "Review alumni mentor profiles and session compliance",
                    icon = Icons.Filled.SupervisorAccount,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToMentorship
                )
                AdminActionTile(
                    title = "Broadcast Announcements",
                    description = "Publish targeted campus push broadcasts & system alerts",
                    icon = Icons.Filled.Campaign,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToAnnouncements
                )
                AdminActionTile(
                    title = "Administrator Profile",
                    description = "View Super Admin credentials and security authorization level",
                    icon = Icons.Filled.AdminPanelSettings,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToProfile
                )
            }
        }

        // 3. System Health Card
        item {
            Text(
                text = "System Health & Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "System Status", fontWeight = FontWeight.SemiBold)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = m.systemStatus,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Database Uptime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = m.databaseUptime, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Active Server Sessions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${m.activeSessions}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Security Alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${m.securityAlerts} Detected", style = MaterialTheme.typography.bodySmall, color = if (m.securityAlerts == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 4. Recent Audit Activity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Audit Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
        }

        if (data.recentAuditLogs.isEmpty()) {
            item {
                Text(
                    text = "No recent administrator actions logged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(data.recentAuditLogs) { log ->
                AuditLogItemRow(log = log)
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AuditLogItemRow(log: AuditLogItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.action.replace("ADMIN_", "").replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${log.actorName} • Target: ${log.targetType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
