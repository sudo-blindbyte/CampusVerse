package com.campusverse.app.ui.screens.student.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.ui.components.CampusVerseErrorMessage
import com.campusverse.app.ui.components.ModulePreScreenScaffold

private val SUGGESTED_SUBJECTS = listOf(
    "Distributed Systems",
    "Compiler Design",
    "Algorithms & DSA",
    "Computer Networks",
    "Database Management",
    "Artificial Intelligence",
    "Operating Systems",
    "Web Technologies",
    "Software Engineering"
)

private val RESOURCE_TYPES = listOf(
    "Verified Notes",
    "Lecture Videos",
    "Practice Questions",
    "Flashcards & Summaries"
)

private val STUDY_HOURS_OPTIONS = listOf(
    "< 1 hour/day",
    "1–2 hours/day",
    "2–4 hours/day",
    "4+ hours/day"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentNotesPreScreen(
    currentUser: AuthenticatedUser?,
    onCompleted: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRoute: ((String) -> Unit)? = null,
    viewModel: StudentNotesPreScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(currentUser) {
        viewModel.initialize(currentUser)
    }

    LaunchedEffect(uiState) {
        if (uiState is StudentNotesPreScreenUiState.Saved) {
            onCompleted()
        }
    }

    val state = uiState as? StudentNotesPreScreenUiState.Content ?: return

    ModulePreScreenScaffold(
        title = if (currentStep == 1) "Study Focus & Subjects" else "Resources & Daily Target",
        subtitle = if (currentStep == 1)
            "Tell us what you are studying to prioritize the most relevant notes."
        else
            "Choose your preferred resource formats and daily study routine.",
        whyWeAsk = "CampusVerse ranks verified notes, recommended flashcards, and peer contributions based on your current focus subjects.",
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
        continueButtonText = if (currentStep == 1) "Continue to Formats" else "Save & Personalize Notes",
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
            // STEP 1: What are you studying & priority subjects
            Text(
                text = "What are you currently studying?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.studyingField,
                onValueChange = { viewModel.updateStudyingField(it) },
                label = { Text("Field / Branch / Specialization") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Which subjects are highest priority?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selected subjects will appear first in your notes feed and search filters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SUGGESTED_SUBJECTS.forEach { subject ->
                    val isSelected = state.selectedPrioritySubjects.contains(subject)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.togglePrioritySubject(subject) },
                        label = { Text(subject) },
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
        } else {
            // STEP 2: Resource Types & Daily Study Hours
            Text(
                text = "What type of study resources do you prefer?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RESOURCE_TYPES.forEach { type ->
                    val isSelected = state.selectedResourceTypes.contains(type)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleResourceType(type) },
                        label = { Text(type) },
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
                text = "How much time do you usually study?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                STUDY_HOURS_OPTIONS.forEach { hours ->
                    val isSelected = state.dailyStudyTime == hours
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateDailyStudyTime(hours) },
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
                                text = hours,
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
        }
    }
}
