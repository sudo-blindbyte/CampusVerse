package com.campusverse.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.campusverse.app.data.model.UserRole
import com.campusverse.app.data.repository.AuthRepositoryImpl
import com.campusverse.app.domain.auth.AuthRepository
import com.campusverse.app.domain.auth.AuthState
import com.campusverse.app.ui.screens.admin.AdminDashboardScreen
import com.campusverse.app.ui.screens.admin.AdminLoginScreen
import com.campusverse.app.ui.screens.admin.AdminProfileScreen
import com.campusverse.app.ui.screens.admin.eventsjobs.AdminEventsScreen
import com.campusverse.app.ui.screens.admin.eventsjobs.AdminJobsScreen
import com.campusverse.app.ui.screens.alumni.AlumniHomeScreen
import com.campusverse.app.ui.screens.alumni.account.AlumniAccountRecoveryScreen
import com.campusverse.app.ui.screens.alumni.account.AlumniMyProfileScreen
import com.campusverse.app.ui.screens.alumni.account.AlumniNotificationsScreen
import com.campusverse.app.ui.screens.alumni.account.AlumniPrivacySettingsScreen
import com.campusverse.app.ui.screens.alumni.account.AlumniSecuritySettingsScreen
import com.campusverse.app.ui.screens.alumni.account.AlumniSettingsScreen
import com.campusverse.app.ui.screens.alumni.account.CareerPreferencesScreen
import com.campusverse.app.ui.screens.alumni.careerdev.AiCareerAssistantScreen
import com.campusverse.app.ui.screens.alumni.careerdev.CareerRoadmapScreen
import com.campusverse.app.ui.screens.alumni.careerdev.InterviewPrepScreen
import com.campusverse.app.ui.screens.alumni.careerdev.MockInterviewScreen
import com.campusverse.app.ui.screens.alumni.careerdev.SkillDevelopmentScreen
import com.campusverse.app.ui.screens.alumni.careers.ApplicationTrackingScreen
import com.campusverse.app.ui.screens.alumni.careers.CareersScreen
import com.campusverse.app.ui.screens.alumni.careers.CompanyJobsScreen
import com.campusverse.app.ui.screens.alumni.careers.CompanyProfileScreen
import com.campusverse.app.ui.screens.alumni.careers.JobDetailScreen
import com.campusverse.app.ui.screens.alumni.careers.ReferralsScreen
import com.campusverse.app.ui.screens.alumni.careers.SavedJobsScreen
import com.campusverse.app.ui.screens.alumni.events.AlumniEventsScreen
import com.campusverse.app.ui.screens.alumni.legal.AboutScreen
import com.campusverse.app.ui.screens.alumni.legal.CommunityGuidelinesScreen
import com.campusverse.app.ui.screens.alumni.legal.HelpSupportScreen
import com.campusverse.app.ui.screens.alumni.legal.PrivacyPolicyScreen
import com.campusverse.app.ui.screens.alumni.legal.TermsScreen
import com.campusverse.app.ui.screens.alumni.mentorship.AlumniMentorshipScreen
import com.campusverse.app.ui.screens.alumni.mentorship.MentorProfileScreen
import com.campusverse.app.ui.screens.alumni.mentorship.MentorshipRequestsScreen
import com.campusverse.app.ui.screens.alumni.mentorship.SessionDetailScreen
import com.campusverse.app.ui.screens.alumni.messaging.AlumniChatScreen
import com.campusverse.app.ui.screens.alumni.messaging.AlumniMessagesScreen
import com.campusverse.app.ui.screens.alumni.network.AlumniConnectionsScreen
import com.campusverse.app.ui.screens.alumni.network.AlumniNetworkScreen
import com.campusverse.app.ui.screens.alumni.network.AlumniProfileDetailScreen
import com.campusverse.app.ui.screens.aspirant.AspirantHomeScreen
import com.campusverse.app.ui.screens.createaccount.CreateAccountScreen
import com.campusverse.app.ui.screens.createaccount.RegisterViewModel
import com.campusverse.app.ui.screens.forgotpassword.ForgotPasswordScreen
import com.campusverse.app.ui.screens.forgotpassword.ForgotPasswordViewModel
import com.campusverse.app.ui.screens.login.LoginScreen
import com.campusverse.app.ui.screens.resetpassword.ResetPasswordScreen
import com.campusverse.app.ui.screens.roleselection.RoleSelectionScreen
import com.campusverse.app.ui.screens.splash.SplashScreen
import com.campusverse.app.ui.screens.student.StudentHomeScreen
import com.campusverse.app.ui.screens.student.academics.AcademicsScreen
import com.campusverse.app.ui.screens.student.ai.AiStudyAssistantScreen
import com.campusverse.app.ui.screens.student.community.CommunityDetailScreen
import com.campusverse.app.ui.screens.student.community.CommunityScreen
import com.campusverse.app.ui.screens.student.community.CommunityViewModel
import com.campusverse.app.ui.screens.student.events.EventsScreen
import com.campusverse.app.ui.screens.student.library.LibraryScreen
import com.campusverse.app.ui.screens.student.marketplace.MarketplaceScreen
import com.campusverse.app.ui.screens.student.marketplace.MarketplaceViewModel
import com.campusverse.app.ui.screens.student.notes.NotesScreen
import com.campusverse.app.ui.screens.student.notes.NotesViewModel
import com.campusverse.app.ui.screens.student.profile.StudentProfileScreen
import com.campusverse.app.ui.screens.student.settings.StudentSettingsScreen
import com.campusverse.app.ui.screens.verification.VerificationScreen
import com.campusverse.app.ui.screens.verification.VerificationSuccessScreen
import com.campusverse.app.ui.screens.welcome.WelcomeScreen
import kotlinx.coroutines.launch

/**
 * Main navigation host configuring all CampusVerse routes, role dashboards, and route guards.
 */
@Composable
fun CampusVerseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = AuthRepositoryImpl.instance,
    startDestination: String = Screen.Splash.route
) {
    val authState by authRepository.authState.collectAsStateWithLifecycle()
    val currentUser = (authState as? AuthState.Authenticated)?.user

    // Shared ViewModels for multi-step flows
    val registerViewModel: RegisterViewModel = viewModel()
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
    val notesViewModel: NotesViewModel = viewModel()
    val communityViewModel: CommunityViewModel = viewModel()
    val marketplaceViewModel: MarketplaceViewModel = viewModel()

    val coroutineScope = rememberCoroutineScope()
    val handleLogout: () -> Unit = {
        coroutineScope.launch {
            val userId = currentUser?.userId ?: ""
            authRepository.logout()
            com.campusverse.app.data.repository.UserProfileRepository.instance.clear()
            if (userId.isNotBlank()) {
                com.campusverse.app.data.preferences.ModulePreferencesManager.getInstance(navController.context).clearUserData(userId)
            }
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToDestination = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                authRepository = authRepository
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = { navController.navigate(Screen.RoleSelection.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { user ->
                    val destination = when (user.role) {
                        UserRole.ASPIRANT -> Screen.AspirantHome.route
                        UserRole.STUDENT -> Screen.StudentHome.route
                        UserRole.ALUMNI -> Screen.AlumniHome.route
                        UserRole.ADMIN -> if (user.isAdminAuthorized) {
                            Screen.AdminDashboard.route
                        } else {
                            Screen.AdminLogin.route
                        }
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToCreateAccount = {
                    navController.navigate(Screen.CreateAccount.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoleSelection = { navController.navigate(Screen.RoleSelection.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CreateAccount.route) { inclusive = true }
                    }
                },
                viewModel = registerViewModel
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onNavigateToRoute = { destinationRoute ->
                    if (destinationRoute.endsWith("_home") || destinationRoute.startsWith("admin")) {
                        navController.navigate(destinationRoute) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(destinationRoute)
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                registerViewModel = registerViewModel
            )
        }

        composable(Screen.Verification.route) {
            val email = currentUser?.email ?: ""
            VerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.VerificationSuccess.route) {
                        popUpTo(Screen.Verification.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VerificationSuccess.route) {
            VerificationSuccessScreen(
                onContinue = {
                    val destination = when (currentUser?.role) {
                        UserRole.ASPIRANT -> Screen.AspirantHome.route
                        UserRole.STUDENT -> Screen.StudentHome.route
                        UserRole.ALUMNI -> Screen.AlumniHome.route
                        UserRole.ADMIN -> if (currentUser.isAdminAuthorized) {
                            Screen.AdminDashboard.route
                        } else {
                            Screen.AdminLogin.route
                        }
                        null -> Screen.Welcome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResetPassword = { navController.navigate(Screen.ResetPassword.route) },
                viewModel = forgotPasswordViewModel
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ---------------------------------------------------------------------
        // Student Module Screens
        // ---------------------------------------------------------------------
        composable(Screen.StudentHome.route) {
            if (currentUser == null) {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate(Screen.RoleSelection.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
                )
            } else {
                StudentHomeScreen(
                    currentUser = currentUser,
                    onNavigateToAcademics = { navController.navigate(Screen.Academics.route) },
                    onNavigateToNotes = { navController.navigate(Screen.NotesHub.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToAiAssistant = { navController.navigate(Screen.AiStudyAssistant.route) },
                    onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                    onNavigateToCommunity = { navController.navigate(Screen.CommunityList.route) },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToProfile = { navController.navigate(Screen.StudentProfile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.StudentSettings.route) },
                    onNavigateToStore = { navController.navigate(Screen.Store.route) },
                    onLogout = handleLogout
                )
            }
        }

        val onNavigateToStudentTab: (String) -> Unit = { route ->
            if (route == Screen.StudentHome.route) {
                navController.navigate(Screen.StudentHome.route) {
                    popUpTo(Screen.StudentHome.route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(route) {
                    popUpTo(Screen.StudentHome.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        composable(Screen.Academics.route) {
            AcademicsScreen(
                currentUser = currentUser,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoute = onNavigateToStudentTab
            )
        }

        composable(Screen.NotesHub.route) {
            NotesScreen(
                currentUser = currentUser,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoute = onNavigateToStudentTab,
                viewModel = notesViewModel
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AiStudyAssistant.route) {
            AiStudyAssistantScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Events.route) {
            EventsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.CommunityList.route) {
            CommunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCommunityDetail = { communityId ->
                    navController.navigate(Screen.CommunityDetail.createRoute(communityId))
                },
                onNavigateToRoute = onNavigateToStudentTab,
                viewModel = communityViewModel
            )
        }

        composable(
            route = Screen.CommunityDetail.route,
            arguments = listOf(
                navArgument("communityId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
            CommunityDetailScreen(
                communityId = communityId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = communityViewModel
            )
        }

        composable(Screen.Marketplace.route) {
            MarketplaceScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoute = onNavigateToStudentTab,
                viewModel = marketplaceViewModel
            )
        }

        composable(Screen.StudentProfile.route) {
            StudentProfileScreen(
                currentUser = currentUser,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoute = onNavigateToStudentTab
            )
        }

        composable(Screen.StudentSettings.route) {
            StudentSettingsScreen(
                currentUser = currentUser,
                onLogout = handleLogout,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAcademicsPreferences = { navController.navigate(Screen.Academics.route) }
            )
        }

        // ---------------------------------------------------------------------
        // Alumni Module Screens (Core, Careers, Mentorship, Events, Dev, Messaging, Account, Settings, Legal)
        // ---------------------------------------------------------------------
        composable(Screen.AlumniHome.route) {
            AlumniHomeScreen(
                currentUser = currentUser,
                onNavigate = { navController.navigate(it) }
            )
        }

        composable(Screen.AlumniNetwork.route) {
            AlumniNetworkScreen(onNavigate = { navController.navigate(it) })
        }

        composable(Screen.AlumniConnections.route) {
            AlumniConnectionsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AlumniProfileDetail.route,
            arguments = listOf(navArgument("alumniId") { type = NavType.StringType })
        ) { backStackEntry ->
            val alumniId = backStackEntry.arguments?.getString("alumniId") ?: ""
            AlumniProfileDetailScreen(
                alumniId = alumniId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Careers
        composable(Screen.AlumniCareers.route) {
            CareersScreen(onNavigate = { navController.navigate(it) })
        }

        composable(
            route = Screen.AlumniJobDetails.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailScreen(
                jobId = jobId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniSavedJobs.route) {
            SavedJobsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniApplicationTracking.route) {
            ApplicationTrackingScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniReferrals.route) {
            ReferralsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AlumniCompanyProfile.route,
            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
            CompanyProfileScreen(
                companyId = companyId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AlumniCompanyJobs.route,
            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
            CompanyJobsScreen(
                companyId = companyId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mentorship
        composable(Screen.AlumniMentorship.route) {
            AlumniMentorshipScreen(onNavigate = { navController.navigate(it) })
        }

        composable(
            route = Screen.AlumniMentorProfile.route,
            arguments = listOf(navArgument("mentorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mentorId = backStackEntry.arguments?.getString("mentorId") ?: ""
            MentorProfileScreen(
                mentorId = mentorId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniMentorshipRequests.route) {
            MentorshipRequestsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AlumniSessionDetails.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionDetailScreen(
                sessionId = sessionId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Events
        composable(Screen.AlumniEvents.route) {
            AlumniEventsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Career Development
        composable(Screen.AlumniAiCareerAssistant.route) {
            AiCareerAssistantScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniCareerRoadmap.route) {
            CareerRoadmapScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniSkillDevelopment.route) {
            SkillDevelopmentScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniInterviewPrep.route) {
            InterviewPrepScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniMockInterview.route) {
            MockInterviewScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Messaging
        composable(Screen.AlumniMessages.route) {
            AlumniMessagesScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AlumniChat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            AlumniChatScreen(
                conversationId = conversationId,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Account & Settings
        composable(Screen.AlumniProfile.route) {
            AlumniMyProfileScreen(
                currentUser = currentUser,
                onNavigate = { navController.navigate(it) }
            )
        }

        composable(Screen.AlumniSettings.route) {
            AlumniSettingsScreen(
                currentUser = currentUser,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() },
                onLogout = handleLogout
            )
        }

        composable(Screen.AlumniCareerPreferences.route) {
            CareerPreferencesScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniNotifications.route) {
            AlumniNotificationsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniPrivacySettings.route) {
            AlumniPrivacySettingsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniSecuritySettings.route) {
            AlumniSecuritySettingsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AlumniAccountRecovery.route) {
            AlumniAccountRecoveryScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() }
            )
        }

        // Help & Legal
        composable(Screen.AlumniHelpSupport.route) {
            HelpSupportScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AlumniAbout.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AlumniTerms.route) {
            TermsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AlumniPrivacyPolicy.route) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AlumniCommunityGuidelines.route) {
            CommunityGuidelinesScreen(onBack = { navController.popBackStack() })
        }

        // ---------------------------------------------------------------------
        // Phase 6: Aspirant Module Navigation Graph
        // ---------------------------------------------------------------------
        composable(Screen.AspirantHome.route) {
            if (currentUser == null) {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate(Screen.RoleSelection.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
                )
            } else {
                AspirantHomeScreen(
                    currentUser = currentUser,
                    onNavigateToCollegeExplorer = { navController.navigate(Screen.AspirantCollegeExplorer.route) },
                    onNavigateToCollegeComparison = { navController.navigate(Screen.AspirantCollegeComparison.route) },
                    onNavigateToPredictor = { navController.navigate(Screen.AspirantPredictor.route) },
                    onNavigateToScholarships = { navController.navigate(Screen.AspirantScholarships.route) },
                    onNavigateToSavedScholarships = { navController.navigate(Screen.AspirantSavedScholarships.route) },
                    onNavigateToAiRecommendations = { navController.navigate(Screen.AspirantAiRecommendations.route) },
                    onNavigateToProfile = { navController.navigate(Screen.AspirantProfile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.AspirantSettings.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.AspirantNotifications.route) },
                    onNavigateToStore = { navController.navigate(Screen.Store.route) }
                )
            }
        }

        composable(Screen.AspirantCollegeExplorer.route) {
            com.campusverse.app.ui.screens.aspirant.colleges.CollegeExplorerScreen(
                onNavigateToComparison = { navController.navigate(Screen.AspirantCollegeComparison.route) },
                onNavigateToHome = { navController.navigate(Screen.AspirantHome.route) },
                onNavigateToScholarships = { navController.navigate(Screen.AspirantScholarships.route) },
                onNavigateToPredictor = { navController.navigate(Screen.AspirantPredictor.route) },
                onNavigateToProfile = { navController.navigate(Screen.AspirantProfile.route) }
            )
        }

        composable(Screen.AspirantCollegeComparison.route) {
            com.campusverse.app.ui.screens.aspirant.colleges.CollegeComparisonScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AspirantPredictor.route) {
            com.campusverse.app.ui.screens.aspirant.predictor.AdmissionPredictorScreen(
                onNavigateToHome = { navController.navigate(Screen.AspirantHome.route) },
                onNavigateToColleges = { navController.navigate(Screen.AspirantCollegeExplorer.route) },
                onNavigateToScholarships = { navController.navigate(Screen.AspirantScholarships.route) },
                onNavigateToProfile = { navController.navigate(Screen.AspirantProfile.route) }
            )
        }

        composable(Screen.AspirantScholarships.route) {
            com.campusverse.app.ui.screens.aspirant.scholarships.ScholarshipFinderScreen(
                onNavigateToSavedScholarships = { navController.navigate(Screen.AspirantSavedScholarships.route) },
                onNavigateToHome = { navController.navigate(Screen.AspirantHome.route) },
                onNavigateToColleges = { navController.navigate(Screen.AspirantCollegeExplorer.route) },
                onNavigateToPredictor = { navController.navigate(Screen.AspirantPredictor.route) },
                onNavigateToProfile = { navController.navigate(Screen.AspirantProfile.route) }
            )
        }

        composable(Screen.AspirantSavedScholarships.route) {
            com.campusverse.app.ui.screens.aspirant.scholarships.SavedScholarshipsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AspirantAiRecommendations.route) {
            com.campusverse.app.ui.screens.aspirant.ai.AiRecommendationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AspirantProfile.route) {
            com.campusverse.app.ui.screens.aspirant.profile.AspirantProfileScreen(
                currentUser = currentUser,
                onNavigateToHome = { navController.navigate(Screen.AspirantHome.route) },
                onNavigateToColleges = { navController.navigate(Screen.AspirantCollegeExplorer.route) },
                onNavigateToScholarships = { navController.navigate(Screen.AspirantScholarships.route) },
                onNavigateToPredictor = { navController.navigate(Screen.AspirantPredictor.route) },
                onNavigateToSettings = { navController.navigate(Screen.AspirantSettings.route) }
            )
        }

        composable(Screen.AspirantSettings.route) {
            com.campusverse.app.ui.screens.aspirant.settings.AspirantSettingsScreen(
                currentUser = currentUser,
                onNavigateBack = { navController.popBackStack() },
                onLogout = handleLogout
            )
        }

        composable(Screen.AspirantNotifications.route) {
            com.campusverse.app.ui.screens.aspirant.account.AspirantNotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------------
        // Admin Module Screens
        // ---------------------------------------------------------------------
        composable(Screen.AdminLogin.route) {
            AdminLoginScreen(
                currentUser = currentUser,
                onNavigateToDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminLogin.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDashboard.route) {
            if (currentUser == null || currentUser.role != UserRole.ADMIN || !currentUser.isAdminAuthorized) {
                AdminLoginScreen(
                    currentUser = currentUser,
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateBack = { navController.navigate(Screen.Welcome.route) }
                )
            } else {
                AdminDashboardScreen(
                    currentUser = currentUser,
                    onLogout = handleLogout,
                    onNavigateToUsers = { navController.navigate(Screen.AdminUserManagement.route) },
                    onNavigateToReports = { navController.navigate(Screen.AdminReports.route) },
                    onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) },
                    onNavigateToVerification = { navController.navigate(Screen.AdminVerification.route) },
                    onNavigateToMarketplace = { navController.navigate(Screen.AdminMarketplace.route) },
                    onNavigateToEventsJobs = { navController.navigate(Screen.AdminEvents.route) },
                    onNavigateToJobs = { navController.navigate(Screen.AdminJobs.route) },
                    onNavigateToMentorship = { navController.navigate(Screen.AdminMentorship.route) },
                    onNavigateToAnnouncements = { navController.navigate(Screen.AdminAnnouncements.route) },
                    onNavigateToProfile = { navController.navigate(Screen.AdminProfile.route) },
                    onNavigateToNotes = { navController.navigate(Screen.AdminNotes.route) },
                    onNavigateToFinance = { navController.navigate(Screen.AdminFinance.route) }
                )
            }
        }

        composable(Screen.AdminUserManagement.route) {
            com.campusverse.app.ui.screens.admin.users.AdminUserManagementScreen(
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToReports = { navController.navigate(Screen.AdminReports.route) },
                onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) }
            )
        }

        composable(Screen.AdminVerification.route) {
            com.campusverse.app.ui.screens.admin.verification.AdminVerificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminReports.route) {
            com.campusverse.app.ui.screens.admin.reports.AdminReportsScreen(
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToUsers = { navController.navigate(Screen.AdminUserManagement.route) },
                onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) }
            )
        }

        composable(Screen.AdminMarketplace.route) {
            com.campusverse.app.ui.screens.admin.marketplace.AdminMarketplaceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminEvents.route) {
            AdminEventsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminJobs.route) {
            AdminJobsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminMentorship.route) {
            com.campusverse.app.ui.screens.admin.mentorship.AdminMentorshipScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminAnnouncements.route) {
            com.campusverse.app.ui.screens.admin.announcements.AdminAnnouncementsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminSettings.route) {
            com.campusverse.app.ui.screens.admin.settings.AdminSettingsScreen(
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToUsers = { navController.navigate(Screen.AdminUserManagement.route) },
                onNavigateToReports = { navController.navigate(Screen.AdminReports.route) },
                onNavigateToProfile = { navController.navigate(Screen.AdminProfile.route) },
                onLogout = handleLogout
            )
        }

        composable(Screen.AdminProfile.route) {
            AdminProfileScreen(
                currentUser = currentUser,
                onNavigateBack = { navController.popBackStack() },
                onLogout = handleLogout
            )
        }

        composable(Screen.AdminNotes.route) {
            com.campusverse.app.ui.screens.admin.notes.AdminNotesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Payment & Monetization Screens
        composable(Screen.Store.route) {
            val userRole = currentUser?.role?.name
            com.campusverse.app.ui.screens.payment.StoreScreen(
                userRole = userRole,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTransactions = { navController.navigate(Screen.MyTransactions.route) },
                onNavigateToEntitlements = { navController.navigate(Screen.MyEntitlements.route) }
            )
        }

        composable(Screen.MyTransactions.route) {
            com.campusverse.app.ui.screens.payment.MyTransactionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MyEntitlements.route) {
            com.campusverse.app.ui.screens.payment.MyEntitlementsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminFinance.route) {
            com.campusverse.app.ui.screens.admin.finance.AdminFinanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

