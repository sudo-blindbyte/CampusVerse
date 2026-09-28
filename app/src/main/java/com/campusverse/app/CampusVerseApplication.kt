package com.campusverse.app

import android.app.Application
import com.campusverse.app.data.account.DataStoreAccountStore
import com.campusverse.app.data.account.StoredAccount
import com.campusverse.app.data.account.accountsDataStore
import com.campusverse.app.data.local.PasswordHasher
import com.campusverse.app.data.model.UserRole
import com.campusverse.app.data.repository.AuthRepositoryImpl
import com.campusverse.app.data.session.DataStoreSessionManager
import com.campusverse.app.data.session.sessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Custom Application class for CampusVerse initializing DataStore-backed
 * persistent session and account repositories.
 */
class CampusVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val sessionManager = DataStoreSessionManager(sessionDataStore)
        val accountStore = DataStoreAccountStore(accountsDataStore)

        val defaultApiBaseUrl = com.campusverse.app.data.network.ApiConfig.BASE_URL
        val adminApiBaseUrl = com.campusverse.app.data.network.ApiConfig.ADMIN_BASE_URL

        AuthRepositoryImpl.instance = com.campusverse.app.data.repository.NetworkAuthRepository(
            baseUrl = defaultApiBaseUrl,
            sessionManager = sessionManager,
            accountStore = accountStore
        )

        val studentRepo = com.campusverse.app.data.repository.NetworkStudentRepository(baseUrl = defaultApiBaseUrl, sessionManager = sessionManager)
        val alumniRepo = com.campusverse.app.data.repository.NetworkAlumniRepository(baseUrl = defaultApiBaseUrl, sessionManager = sessionManager)
        val aspirantRepo = com.campusverse.app.data.repository.NetworkAspirantRepository(baseUrl = defaultApiBaseUrl, sessionManager = sessionManager)
        val adminRepo = com.campusverse.app.data.repository.NetworkAdminRepository(baseUrl = adminApiBaseUrl, sessionManager = sessionManager)
        val paymentRepo = com.campusverse.app.data.repository.NetworkPaymentRepository(baseUrl = defaultApiBaseUrl, sessionManager = sessionManager)
        val modulePrefsManager = com.campusverse.app.data.preferences.ModulePreferencesManager.getInstance(this)

        com.campusverse.app.data.repository.NetworkStudentRepository.instance = studentRepo
        com.campusverse.app.data.repository.NetworkAlumniRepository.instance = alumniRepo
        com.campusverse.app.data.repository.NetworkAspirantRepository.instance = aspirantRepo
        com.campusverse.app.data.repository.NetworkAdminRepository.instance = adminRepo
        com.campusverse.app.data.repository.NetworkPaymentRepository.instance = paymentRepo

        com.campusverse.app.data.repository.UserProfileRepository.instance =
            com.campusverse.app.data.repository.UserProfileRepository(
                sessionManager = sessionManager,
                studentRepository = studentRepo,
                alumniRepository = alumniRepo,
                aspirantRepository = aspirantRepo,
                modulePreferencesManager = modulePrefsManager
            )

        // Seed demo accounts asynchronously if not present
        CoroutineScope(Dispatchers.IO).launch {
            seedDemoAccounts(accountStore)
        }
    }

    private suspend fun seedDemoAccounts(accountStore: DataStoreAccountStore) {
        val demoAccounts = listOf(
            Triple("admin@campusverse.edu", "Super Administrator", UserRole.ADMIN to true),
            Triple("student@campusverse.edu", "keval goswami", UserRole.STUDENT to false),
            Triple("alumni@campusverse.edu", "Dr. Aisha Patel", UserRole.ALUMNI to false),
            Triple("aspirant@campusverse.edu", "Kavya Sharma", UserRole.ASPIRANT to false),
            Triple("admin@campusverse.local", "Super Administrator", UserRole.ADMIN to true),
            Triple("student@campusverse.local", "keval goswami", UserRole.STUDENT to false),
            Triple("alumni@campusverse.local", "Dr. Aisha Patel", UserRole.ALUMNI to false),
            Triple("aspirant@campusverse.local", "Kavya Sharma", UserRole.ASPIRANT to false),
            Triple("admin@campusverse.demo", "Super Administrator", UserRole.ADMIN to true),
            Triple("student@campusverse.demo", "keval goswami", UserRole.STUDENT to false)
        )

        for ((email, name, roleInfo) in demoAccounts) {
            val (role, isAdminAuthorized) = roleInfo
            if (accountStore.getAccount(email) == null) {
                val salt = UUID.randomUUID().toString()
                val passwordHash = PasswordHasher.hash("Password123", salt)
                accountStore.saveAccount(
                    StoredAccount(
                        userId = "demo_${role.name.lowercase()}",
                        name = name,
                        email = email,
                        passwordHash = passwordHash,
                        passwordSalt = salt,
                        role = role,
                        isEmailVerified = true,
                        isAdminAuthorized = isAdminAuthorized
                    )
                )
            }
        }
    }
}
