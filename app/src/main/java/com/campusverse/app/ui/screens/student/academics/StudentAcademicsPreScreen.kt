package com.campusverse.app.ui.screens.student.academics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.ui.components.CampusVerseErrorMessage
import com.campusverse.app.ui.components.ModulePreScreenScaffold

private val DEGREE_OPTIONS = listOf("B.Tech", "BCA", "B.Sc", "M.Tech", "MCA", "MBA")
private val BRANCH_OPTIONS = listOf(
    "Computer Science and Engineering",
    "Information Technology",
    "Artificial Intelligence & ML",
    "Electronics & Comm.",
    "Mechanical Engineering"
)
private val GOAL_OPTIONS = listOf(
    "Placement Prep & Top Tech Jobs",
    "High CGPA (>9.0)",
    "Research & Higher Studies (MS/PhD)",
    "Competitive Exams (GATE/CAT)"
)
private val FOCUS_OPTIONS = listOf(
    "Algorithms & Data Structures",
    "System Design & Distributed Systems",
    "AI & Machine Learning",
    "Web & Mobile Development",
    "Database Management Systems",
    "Computer Networks & Security",
    "Cloud Computing & DevOps"
)
private val DIFFICULTY_OPTIONS = listOf("Beginner", "Intermediate", "Advanced")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentAcademicsPreScreen(
    currentUser: AuthenticatedUser?,
    onCompleted: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRoute: ((String) -> Unit)? = null,
    viewModel: StudentAcademicsPreScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(currentUser) {
        viewModel.initialize(currentUser)
    }

    LaunchedEffect(uiState) {
        if (uiState is StudentAcademicsPreScreenUiState.Saved) {
            onCompleted()
        }
    }

    val state = uiState as? StudentAcademicsPreScreenUiState.Content ?: return

    ModulePreScreenScaffold(
        title = if (currentStep == 1) "Academic Profile & Goals" else "Study Focus & Difficulty",
        subtitle = if (currentStep == 1)
            "Select your academic level and main targets to personalize courses."
        else
            "Choose your focus subjects and comfort level to tailor AI study alerts.",
        whyWeAsk = "CampusVerse customizes your academic overview, study alert cards, and course recommendations based on these preferences.",
        currentStep = currentStep,
        totalSteps = 2,
        onNavigateBack = if (currentStep > 1) { { currentStep = 1 } } else onNavigateBack,
        onSkip = if (currentStep == 2) { { viewModel.savePreferences(currentUser) } } else null,
        onContinue = {
            if (currentStep == 1) {
                currentStep = 2
            } else {
                viewModel.savePreferences(currentUser)
            }
        },
        continueButtonText = if (currentStep == 1) "Continue to Focus Areas" else "Save & Personalize Academics",
        isLoading = state.isSaving,
        bottomBarContent = {
            com.campusverse.app.ui.screens.student.components.StudentBottomBar(
                currentRoute = com.campusverse.app.navigation.Screen.Academics.route,
                onNavigate = { route -> onNavigateToRoute?.invoke(route) }
            )
        }
    ) {
        if (state.errorMessage != null) {
            CampusVerseErrorMessage(message = state.errorMessage)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (currentStep == 1) {
            // STEP 1: Degree, Semester, Branch, Goal
            Text(
                text = "Current Degree / Program",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEGREE_OPTIONS.forEach { degree ->
                    FilterChip(
                        selected = state.degree == degree,
                        onClick = { viewModel.updateDegree(degree) },
                        label = { Text(degree) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Current Semester (${state.semester})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..8).forEach { sem ->
                    FilterChip(
                        selected = state.semester == sem,
                        onClick = { viewModel.updateSemester(sem) },
                        label = { Text("Sem $sem") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Department / Specialization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BRANCH_OPTIONS.forEach { branch ->
                    val isSelected = state.branch == branch
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateBranch(branch) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = branch,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Primary Academic Target",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GOAL_OPTIONS.forEach { goal ->
                    val isSelected = state.academicGoal == goal
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateGoal(goal) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = goal,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // STEP 2: Focus Areas, Difficulty, GPA
            Text(
                text = "Preferred Study & Focus Areas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select all areas you want personalized study material for.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FOCUS_OPTIONS.forEach { focus ->
                    val isSelected = state.selectedFocusAreas.contains(focus)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleFocusArea(focus) },
                        label = { Text(focus) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Current Course Difficulty Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DIFFICULTY_OPTIONS.forEach { diff ->
                    val isSelected = state.difficultyLevel == diff
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateDifficulty(diff) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = diff,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Current CGPA / Percentage (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Used only for academic milestone recommendations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.recentGpa,
                onValueChange = { viewModel.updateGpa(it) },
                label = { Text("CGPA (0.0 - 10.0)") },
                placeholder = { Text("e.g. 8.75") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
