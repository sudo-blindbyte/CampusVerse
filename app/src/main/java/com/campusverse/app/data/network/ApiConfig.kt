package com.campusverse.app.data.network

import android.os.Build

/**
 * Production Centralized API Configuration for CampusVerse.
 * Allows effortless switching between Production HTTPS Cloud Environment,
 * Local Staging, and Emulator / Physical USB Development.
 */
object ApiConfig {

    /**
     * Primary Production HTTPS Cloud Base URL.
     */
    const val PRODUCTION_BASE_URL = "https://api.campusverse.edu/api/v1"

    /**
     * Staging / Internal Local Development Fallback Base URL.
     */
    const val STAGING_BASE_URL = "https://staging-api.campusverse.edu/api/v1"

    /**
     * Custom development base URL for physical PC Wi-Fi / USB testing.
     */
    var DEV_URL_OVERRIDE: String? = "http://192.168.56.1:4000/api/v1"

    /**
     * Development Base URL (supports PC IP 192.168.56.1, Emulator 10.0.2.2, and Custom IP).
     */
    val DEVELOPMENT_BASE_URL: String
        get() {
            if (!DEV_URL_OVERRIDE.isNullOrBlank()) {
                return DEV_URL_OVERRIDE!!
            }
            val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu")
            return if (isEmulator) "http://10.0.2.2:4000/api/v1" else "http://192.168.56.1:4000/api/v1"
        }

    /**
     * Active API Base URL used by all Android repositories.
     */
    var USE_PRODUCTION_API: Boolean = false

    val BASE_URL: String
        get() = if (USE_PRODUCTION_API) PRODUCTION_BASE_URL else DEVELOPMENT_BASE_URL

    val ADMIN_BASE_URL: String
        get() = "$BASE_URL/admin"
}
