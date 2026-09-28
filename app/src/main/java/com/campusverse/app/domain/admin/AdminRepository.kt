package com.campusverse.app.domain.admin

import com.campusverse.app.data.model.AdminAnnouncementItem
import com.campusverse.app.data.model.AdminDashboardData
import com.campusverse.app.data.model.AdminEventItem
import com.campusverse.app.data.model.AdminJobItem
import com.campusverse.app.data.model.AdminMarketplaceItem
import com.campusverse.app.data.model.AdminMentorItem
import com.campusverse.app.data.model.AdminPlatformSettingsData
import com.campusverse.app.data.model.AdminReportItem
import com.campusverse.app.data.model.AdminUserItem
import com.campusverse.app.data.model.AdminVerificationItem
import com.campusverse.app.data.model.NoteItem

interface AdminRepository {
    // 1. Dashboard
    suspend fun getDashboardData(): Result<AdminDashboardData>

    // 2. User Management
    suspend fun getUsers(search: String? = null, role: String? = null, status: String? = null): Result<List<AdminUserItem>>
    suspend fun getUserDetails(userId: String): Result<AdminUserItem>
    suspend fun updateUserStatus(userId: String, isActive: Boolean? = null, role: String? = null, reason: String? = null): Result<AdminUserItem>
    suspend fun resetUserPassword(userId: String): Result<String>
    suspend fun createUser(name: String, email: String, password: String, role: String): Result<AdminUserItem>
    suspend fun deleteUser(userId: String): Result<Boolean>

    // 3. Verifications
    suspend fun getVerifications(status: String? = null): Result<List<AdminVerificationItem>>
    suspend fun reviewVerification(verificationId: String, status: String, rejectionReason: String? = null): Result<AdminVerificationItem>

    // 4. Reports & Moderation
    suspend fun getReports(status: String? = null, targetType: String? = null): Result<List<AdminReportItem>>
    suspend fun resolveReport(reportId: String, status: String, actionTaken: String, notes: String? = null): Result<AdminReportItem>

    // 5. Marketplace
    suspend fun getMarketplaceListings(search: String? = null, category: String? = null): Result<List<AdminMarketplaceItem>>
    suspend fun moderateMarketplaceItem(itemId: String, action: String, reason: String? = null): Result<Boolean>

    // 6. Events & Jobs
    suspend fun getEvents(): Result<List<AdminEventItem>>
    suspend fun moderateEvent(eventId: String, action: String, reason: String? = null): Result<Boolean>
    suspend fun getJobs(): Result<List<AdminJobItem>>
    suspend fun moderateJob(jobId: String, action: String, reason: String? = null): Result<Boolean>

    // 7. Mentorship
    suspend fun getMentors(): Result<List<AdminMentorItem>>
    suspend fun moderateMentor(mentorId: String, action: String, reason: String? = null): Result<Boolean>

    // 8. Announcements
    suspend fun getAnnouncements(): Result<List<AdminAnnouncementItem>>
    suspend fun createAnnouncement(title: String, content: String, targetRole: String? = null, priority: String = "NORMAL"): Result<AdminAnnouncementItem>

    // 9. Platform Settings
    suspend fun getPlatformSettings(): Result<AdminPlatformSettingsData>
    suspend fun updatePlatformSettings(settings: AdminPlatformSettingsData): Result<AdminPlatformSettingsData>

    // 10. Notes Moderation
    suspend fun getAdminNotes(status: String? = null, search: String? = null): Result<List<NoteItem>>
    suspend fun moderateNote(noteId: String, action: String, reason: String? = null): Result<NoteItem>
    suspend fun deleteNotePermanently(noteId: String): Result<Boolean>
}

