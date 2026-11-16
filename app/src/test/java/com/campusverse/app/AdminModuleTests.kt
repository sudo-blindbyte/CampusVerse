package com.campusverse.app

import com.campusverse.app.data.model.AdminAnnouncementItem
import com.campusverse.app.data.model.AdminDashboardData
import com.campusverse.app.data.model.AdminDashboardMetrics
import com.campusverse.app.data.model.AdminEventItem
import com.campusverse.app.data.model.AdminJobItem
import com.campusverse.app.data.model.AdminMarketplaceItem
import com.campusverse.app.data.model.AdminMentorItem
import com.campusverse.app.data.model.AdminPlatformSettingsData
import com.campusverse.app.data.model.AdminReportItem
import com.campusverse.app.data.model.AdminUserItem
import com.campusverse.app.data.model.AdminVerificationItem
import com.campusverse.app.data.model.AuditLogItem
import com.campusverse.app.data.model.NoteItem
import com.campusverse.app.data.model.UserRole
import com.campusverse.app.domain.admin.AdminRepository
import com.campusverse.app.domain.auth.AuthRepository
import com.campusverse.app.domain.auth.AuthenticatedUser
import com.campusverse.app.ui.screens.admin.AdminDashboardUiState
import com.campusverse.app.ui.screens.admin.AdminDashboardViewModel
import com.campusverse.app.ui.screens.admin.AdminLoginUiState
import com.campusverse.app.ui.screens.admin.AdminLoginViewModel
import com.campusverse.app.ui.screens.admin.announcements.AdminAnnouncementsUiState
import com.campusverse.app.ui.screens.admin.announcements.AdminAnnouncementsViewModel
import com.campusverse.app.ui.screens.admin.notes.AdminNotesUiState
import com.campusverse.app.ui.screens.admin.notes.AdminNotesViewModel
import com.campusverse.app.ui.screens.admin.eventsjobs.AdminEventsJobsUiState
import com.campusverse.app.ui.screens.admin.eventsjobs.AdminEventsJobsViewModel
import com.campusverse.app.ui.screens.admin.marketplace.AdminMarketplaceUiState
import com.campusverse.app.ui.screens.admin.marketplace.AdminMarketplaceViewModel
import com.campusverse.app.ui.screens.admin.mentorship.AdminMentorshipUiState
import com.campusverse.app.ui.screens.admin.mentorship.AdminMentorshipViewModel
import com.campusverse.app.ui.screens.admin.reports.AdminReportsUiState
import com.campusverse.app.ui.screens.admin.reports.AdminReportsViewModel
import com.campusverse.app.ui.screens.admin.settings.AdminSettingsUiState
import com.campusverse.app.ui.screens.admin.settings.AdminSettingsViewModel
import com.campusverse.app.ui.screens.admin.users.AdminUserManagementUiState
import com.campusverse.app.ui.screens.admin.users.AdminUserManagementViewModel
import com.campusverse.app.ui.screens.admin.verification.AdminVerificationUiState
import com.campusverse.app.ui.screens.admin.verification.AdminVerificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAdminRepository : AdminRepository {
    var metrics = AdminDashboardMetrics(
        totalUsers = 48,
        studentsCount = 28,
        alumniCount = 14,
        aspirantsCount = 5,
        pendingVerifications = 3,
        activeJobs = 12,
        pendingJobs = 2,
        activeMarketplaceListings = 18,
        pendingReports = 2,
        totalEvents = 8
    )

    var users = mutableListOf(
        AdminUserItem("u1", "student@campusverse.edu", "Rohan Mehta", "STUDENT", true, true, null, "CS Major", "Bangalore", "2026-08-15"),
        AdminUserItem("u2", "alumni@campusverse.edu", "Dr. Aisha Patel", "ALUMNI", true, true, null, "AI Engineer", "Mountain View", "2026-08-10"),
        AdminUserItem("u3", "aspirant@campusverse.edu", "Kavya Sharma", "ASPIRANT", true, false, null, "Aspirant", "Mumbai", "2026-08-20")
    )

    var verifications = mutableListOf(
        AdminVerificationItem("v1", "u3", "aspirant@campusverse.edu", "Kavya Sharma", "ASPIRANT", "GOVERNMENT_ID", "url1", "PENDING", "2026-08-28")
    )

    var reports = mutableListOf(
        AdminReportItem("r1", "u1", "Rohan Mehta", "student@campusverse.edu", "USER", "u3", "Inappropriate behavior", "PENDING", null, null, "2026-08-28")
    )

    var marketplace = mutableListOf(
        AdminMarketplaceItem("m1", "Calculus Book", 45.0, "BOOKS", "AVAILABLE", "Rohan Mehta", "student@campusverse.edu", "2026-08-25")
    )

    var events = mutableListOf(
        AdminEventItem("e1", "Hackathon 2026", "HACKATHON", "2026-09-15", "Main Hall", "UPCOMING", "NIT", "nit@edu")
    )

    var jobs = mutableListOf(
        AdminJobItem("j1", "Software Engineer", "Google", "FULL_TIME", "Bangalore", "ACTIVE", "Aisha Patel", "aisha@google.com")
    )

    var mentors = mutableListOf(
        AdminMentorItem("men1", "u2", "Dr. Aisha Patel", "aisha@google.com", "Senior AI Eng", "Google", "ML", true, 8, 4.9)
    )

    var announcements = mutableListOf(
        AdminAnnouncementItem("a1", "Maintenance", "Tonight 2 AM", "ALL", "HIGH", "Admin", "2026-08-28", 48)
    )

    var settings = AdminPlatformSettingsData()

    override suspend fun getDashboardData(): Result<AdminDashboardData> {
        return Result.success(AdminDashboardData(metrics = metrics))
    }

    override suspend fun getUsers(search: String?, role: String?, status: String?): Result<List<AdminUserItem>> {
        var filtered = users.toList()
        if (!search.isNullOrBlank()) filtered = filtered.filter { it.fullName.contains(search, true) || it.email.contains(search, true) }
        if (!role.isNullOrBlank() && role != "ALL") filtered = filtered.filter { it.role.equals(role, true) }
        if (!status.isNullOrBlank() && status != "ALL") {
            val wantActive = status.equals("ACTIVE", true)
            filtered = filtered.filter { it.isActive == wantActive }
        }
        return Result.success(filtered)
    }

    override suspend fun getUserDetails(userId: String): Result<AdminUserItem> {
        val user = users.find { it.id == userId } ?: users.first()
        return Result.success(user)
    }

    override suspend fun updateUserStatus(userId: String, isActive: Boolean?, role: String?, reason: String?): Result<AdminUserItem> {
        val idx = users.indexOfFirst { it.id == userId }
        if (idx >= 0) {
            val updated = users[idx].copy(
                isActive = isActive ?: users[idx].isActive,
                role = role ?: users[idx].role
            )
            users[idx] = updated
            return Result.success(updated)
        }
        return Result.success(users.first())
    }

    override suspend fun resetUserPassword(userId: String): Result<String> {
        return Result.success("TempPass123!")
    }

    override suspend fun createUser(name: String, email: String, password: String, role: String): Result<AdminUserItem> {
        val user = AdminUserItem("u_new", email, name, role, true, true, null, null, null, "2026-08-28")
        users.add(user)
        return Result.success(user)
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
        users.removeAll { it.id == userId }
        return Result.success(true)
    }

    override suspend fun getVerifications(status: String?): Result<List<AdminVerificationItem>> {
        var filtered = verifications.toList()
        if (!status.isNullOrBlank() && status != "ALL") filtered = filtered.filter { it.status.equals(status, true) }
        return Result.success(filtered)
    }

    override suspend fun reviewVerification(verificationId: String, status: String, rejectionReason: String?): Result<AdminVerificationItem> {
        val idx = verifications.indexOfFirst { it.id == verificationId }
        if (idx >= 0) {
            val updated = verifications[idx].copy(status = status, rejectionReason = rejectionReason)
            verifications[idx] = updated
            return Result.success(updated)
        }
        return Result.success(verifications.first())
    }

    override suspend fun getReports(status: String?, targetType: String?): Result<List<AdminReportItem>> {
        var filtered = reports.toList()
        if (!status.isNullOrBlank() && status != "ALL") filtered = filtered.filter { it.status.equals(status, true) }
        if (!targetType.isNullOrBlank() && targetType != "ALL") filtered = filtered.filter { it.targetType.equals(targetType, true) }
        return Result.success(filtered)
    }

    override suspend fun resolveReport(reportId: String, status: String, actionTaken: String, notes: String?): Result<AdminReportItem> {
        val idx = reports.indexOfFirst { it.id == reportId }
        if (idx >= 0) {
            val updated = reports[idx].copy(status = status, resolutionNotes = notes)
            reports[idx] = updated
            return Result.success(updated)
        }
        return Result.success(reports.first())
    }

    override suspend fun getMarketplaceListings(search: String?, category: String?): Result<List<AdminMarketplaceItem>> {
        return Result.success(marketplace)
    }

    override suspend fun moderateMarketplaceItem(itemId: String, action: String, reason: String?): Result<Boolean> {
        if (action == "REMOVE") marketplace.removeAll { it.id == itemId }
        return Result.success(true)
    }

    override suspend fun getEvents(): Result<List<AdminEventItem>> {
        return Result.success(events)
    }

    override suspend fun moderateEvent(eventId: String, action: String, reason: String?): Result<Boolean> {
        if (action == "REMOVE") events.removeAll { it.id == eventId }
        return Result.success(true)
    }

    override suspend fun getJobs(): Result<List<AdminJobItem>> {
        return Result.success(jobs)
    }

    override suspend fun moderateJob(jobId: String, action: String, reason: String?): Result<Boolean> {
        if (action == "REMOVE") jobs.removeAll { it.id == jobId }
        return Result.success(true)
    }

    override suspend fun getMentors(): Result<List<AdminMentorItem>> {
        return Result.success(mentors)
    }

    override suspend fun moderateMentor(mentorId: String, action: String, reason: String?): Result<Boolean> {
        val idx = mentors.indexOfFirst { it.id == mentorId }
        if (idx >= 0) mentors[idx] = mentors[idx].copy(isAcceptingMentees = action == "APPROVE")
        return Result.success(true)
    }

    override suspend fun getAnnouncements(): Result<List<AdminAnnouncementItem>> {
        return Result.success(announcements.toList())
    }

    override suspend fun createAnnouncement(title: String, content: String, targetRole: String?, priority: String): Result<AdminAnnouncementItem> {
        val item = AdminAnnouncementItem("ann_new", title, content, targetRole, priority, "Admin", "2026-08-28", 48)
        return Result.success(item)
    }

    override suspend fun getPlatformSettings(): Result<AdminPlatformSettingsData> {
        return Result.success(settings)
    }

    override suspend fun updatePlatformSettings(settings: AdminPlatformSettingsData): Result<AdminPlatformSettingsData> {
        this.settings = settings
        return Result.success(settings)
    }

    var notes = mutableListOf(
        NoteItem("n1", "u1", "Rohan Mehta", "CS101", "Intro to CS", "Data Structures", "Trees and Graphs", "url1", listOf("cs"), 10, "2026-08-28", false, "PENDING_REVIEW"),
        NoteItem("n2", "u1", "Rohan Mehta", "CS102", "Algorithms", "Sorting Algorithms", "Quick Sort", "url2", listOf("cs"), 5, "2026-08-28", false, "PUBLISHED")
    )

    override suspend fun getAdminNotes(status: String?, search: String?): Result<List<NoteItem>> {
        var filtered = notes.toList()
        if (!status.isNullOrBlank() && status != "ALL") {
            filtered = filtered.filter { it.status.equals(status, true) }
        }
        if (!search.isNullOrBlank()) {
            filtered = filtered.filter { it.title.contains(search, true) }
        }
        return Result.success(filtered)
    }

    override suspend fun moderateNote(noteId: String, action: String, reason: String?): Result<NoteItem> {
        val idx = notes.indexOfFirst { it.id == noteId }
        val newStatus = when (action) {
            "APPROVE" -> "PUBLISHED"
            "REJECT" -> "REJECTED"
            "REMOVE" -> "REMOVED"
            "RESTORE" -> "PUBLISHED"
            else -> "PUBLISHED"
        }
        val updated = notes[idx].copy(
            status = newStatus,
            rejectionReason = if (action == "REJECT") reason else null,
            removalReason = if (action == "REMOVE") reason else null
        )
        notes[idx] = updated
        return Result.success(updated)
    }

    override suspend fun deleteNotePermanently(noteId: String): Result<Boolean> {
        notes.removeAll { it.id == noteId }
        return Result.success(true)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AdminModuleTests {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeAdminRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAdminRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // 1. Admin Dashboard Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminDashboardViewModel_LoadsMetricsSuccessfully() = runTest {
        val viewModel = AdminDashboardViewModel(fakeRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AdminDashboardUiState.Success)
        val success = state as AdminDashboardUiState.Success
        assertEquals(48, success.data.metrics.totalUsers)
        assertEquals(3, success.data.metrics.pendingVerifications)
        assertEquals(2, success.data.metrics.pendingReports)
    }

    // -------------------------------------------------------------------------
    // 2. User Management Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminUserManagementViewModel_FiltersUsersAndTogglesSuspension() = runTest {
        val viewModel = AdminUserManagementViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminUserManagementUiState.Success
        assertEquals(3, state1.users.size)

        // Filter by STUDENT
        viewModel.onRoleSelected("STUDENT")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminUserManagementUiState.Success
        assertEquals(1, state2.users.size)
        assertEquals("Rohan Mehta", state2.users.first().fullName)

        // Search query filter
        viewModel.onRoleSelected(null)
        viewModel.onSearchQueryChanged("Aisha")
        advanceUntilIdle()
        val stateSearch = viewModel.uiState.value as AdminUserManagementUiState.Success
        assertEquals(1, stateSearch.users.size)
        assertEquals("Dr. Aisha Patel", stateSearch.users.first().fullName)

        // Clear search and suspend user
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        val stateAll = viewModel.uiState.value as AdminUserManagementUiState.Success
        val user = stateAll.users.first()
        viewModel.toggleUserSuspension(user)
        advanceUntilIdle()

        val state3 = viewModel.uiState.value as AdminUserManagementUiState.Success
        assertFalse(state3.users.first { it.id == user.id }.isActive)

        // Reset password
        viewModel.resetPassword(user.id)
        advanceUntilIdle()
        val state4 = viewModel.uiState.value as AdminUserManagementUiState.Success
        assertNotNull(state4.actionFeedback)
        assertTrue(state4.actionFeedback!!.contains("TempPass123!"))
    }

    // -------------------------------------------------------------------------
    // 3. Verification Queue Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminVerificationViewModel_ReviewsAndApproves() = runTest {
        val viewModel = AdminVerificationViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminVerificationUiState.Success
        assertEquals(1, state1.verifications.size)
        assertEquals("PENDING", state1.verifications.first().status)

        // Approve verification
        viewModel.submitReview("v1", "APPROVED")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminVerificationUiState.Success
        assertEquals("APPROVED", state2.verifications.first().status)
    }

    // -------------------------------------------------------------------------
    // 4. Reports & Moderation Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminReportsViewModel_ResolvesReportWithAction() = runTest {
        val viewModel = AdminReportsViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminReportsUiState.Success
        assertEquals(1, state1.reports.size)

        // Resolve report
        viewModel.resolveReport("r1", "RESOLVED", "WARN", "Warning issued to user.")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminReportsUiState.Success
        assertEquals("RESOLVED", state2.reports.first().status)
        assertEquals("Warning issued to user.", state2.reports.first().resolutionNotes)
    }

    // -------------------------------------------------------------------------
    // 5. Marketplace Moderation Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminMarketplaceViewModel_ModeratesAndRemovesListing() = runTest {
        val viewModel = AdminMarketplaceViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminMarketplaceUiState.Success
        assertEquals(1, state1.items.size)

        viewModel.moderateItem("m1", "REMOVE")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminMarketplaceUiState.Success
        assertEquals(0, state2.items.size)
    }

    // -------------------------------------------------------------------------
    // 6. Events & Jobs Oversight Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminEventsJobsViewModel_SwitchesTabsAndModerates() = runTest {
        val viewModel = AdminEventsJobsViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminEventsJobsUiState.Success
        assertEquals(1, state1.events.size)
        assertEquals(1, state1.jobs.size)

        viewModel.selectTab(1)
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminEventsJobsUiState.Success
        assertEquals(1, state2.selectedTab)
    }

    // -------------------------------------------------------------------------
    // 7. Mentorship Management Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminMentorshipViewModel_TogglesMentorStatus() = runTest {
        val viewModel = AdminMentorshipViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminMentorshipUiState.Success
        assertTrue(state1.mentors.first().isAcceptingMentees)

        viewModel.moderateMentor("men1", "SUSPEND")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminMentorshipUiState.Success
        assertFalse(state2.mentors.first().isAcceptingMentees)
    }

    // -------------------------------------------------------------------------
    // 8. Announcements Broadcast Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminAnnouncementsViewModel_PublishesBroadcast() = runTest {
        val viewModel = AdminAnnouncementsViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminAnnouncementsUiState.Success
        assertEquals(1, state1.announcements.size)

        viewModel.publishAnnouncement("Emergency Drill", "Fire drill at 3 PM", "ALL", "URGENT")
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminAnnouncementsUiState.Success
        assertEquals(2, state2.announcements.size)
        assertEquals("Emergency Drill", state2.announcements.first().title)
    }

    // -------------------------------------------------------------------------
    // 9. Admin Platform Settings Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminSettingsViewModel_UpdatesPlatformFlags() = runTest {
        val viewModel = AdminSettingsViewModel(fakeRepo)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value as AdminSettingsUiState.Success
        assertFalse(state1.settings.maintenanceMode)

        viewModel.updateSetting(maintenanceMode = true, allowRegistrations = false)
        advanceUntilIdle()

        val state2 = viewModel.uiState.value as AdminSettingsUiState.Success
        assertTrue(state2.settings.maintenanceMode)
        assertFalse(state2.settings.allowNewRegistrations)
    }

    // -------------------------------------------------------------------------
    // 10. Admin Notes Moderation Tests
    // -------------------------------------------------------------------------
    @Test
    fun testAdminNotesViewModel_loadsAndFiltersNotes() = runTest {
        val viewModel = AdminNotesViewModel(fakeRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value as AdminNotesUiState.Success
        assertEquals(2, state.notes.size)

        viewModel.onStatusFilterSelected("PENDING_REVIEW")
        advanceUntilIdle()

        val filteredState = viewModel.uiState.value as AdminNotesUiState.Success
        assertEquals(1, filteredState.notes.size)
        assertEquals("PENDING_REVIEW", filteredState.notes.first().status)
    }

    @Test
    fun testAdminNotesViewModel_approvesAndRejectsNotes() = runTest {
        val viewModel = AdminNotesViewModel(fakeRepo)
        advanceUntilIdle()

        // Approve n1
        viewModel.moderateNote("n1", "APPROVE")
        advanceUntilIdle()

        var state = viewModel.uiState.value as AdminNotesUiState.Success
        assertEquals("Note approved and published successfully.", state.feedbackMessage)
        assertEquals("PUBLISHED", fakeRepo.notes.first { it.id == "n1" }.status)

        // Reject n2 with reason
        viewModel.moderateNote("n2", "REJECT", "Incomplete documentation")
        advanceUntilIdle()

        state = viewModel.uiState.value as AdminNotesUiState.Success
        assertEquals("Note rejected with reason recorded.", state.feedbackMessage)
        val n2 = fakeRepo.notes.first { it.id == "n2" }
        assertEquals("REJECTED", n2.status)
        assertEquals("Incomplete documentation", n2.rejectionReason)
    }

    @Test
    fun testAdminNotesViewModel_permanentlyDeletesNote() = runTest {
        val viewModel = AdminNotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.deletePermanently("n1")
        advanceUntilIdle()

        val state = viewModel.uiState.value as AdminNotesUiState.Success
        assertEquals("Note permanently deleted from database.", state.feedbackMessage)
        assertFalse(fakeRepo.notes.any { it.id == "n1" })
    }
}

