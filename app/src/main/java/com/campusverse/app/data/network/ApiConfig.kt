package com.campusverse.app.data.network

import android.os.Build

/**
 * Production Centralized API Configuration for CampusVerse.
 * Allows effortless switching between Production HTTPS Cloud Environment,
 * Local Staging, and Emulator Development.
 */
object ApiConfig {

    /**
     * Primary Production HTTPS Cloud Base URL.
     * When deployed to AWS / Render / DigitalOcean / GCP, set this to your domain.
     */
    const val PRODUCTION_BASE_URL = "https://api.campusverse.edu/api/v1"

    /**
     * Staging / Internal Local Development Fallback Base URL.
     */
    const val STAGING_BASE_URL = "https://staging-api.campusverse.edu/api/v1"

    /**
     * Development Localhost Base URL (for Android Emulator / Local testing).
     */
    val DEVELOPMENT_BASE_URL: String
        get() {
            val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu")
            return if (isEmulator) "http://10.0.2.2:4000/api/v1" else "http://localhost:4000/api/v1"
        }

    /**
     * Active API Base URL used by all Android repositories.
     * Change this single property or toggle [USE_PRODUCTION_API] to point the entire app to production.
     */
    var USE_PRODUCTION_API: Boolean = false

    val BASE_URL: String
        get() = if (USE_PRODUCTION_API) PRODUCTION_BASE_URL else DEVELOPMENT_BASE_URL

    val ADMIN_BASE_URL: String
        get() = "$BASE_URL/admin"
}
