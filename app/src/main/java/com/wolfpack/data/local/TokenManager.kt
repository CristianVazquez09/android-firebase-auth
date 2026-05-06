package com.wolfpack.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "auth_secure_prefs"
        private const val KEY_TOKEN = "firebase_token"
        private const val KEY_LOGIN_TIME = "login_timestamp"
        private const val KEY_UID = "user_uid"
        private const val SESSION_DURATION_MS = 3 * 60 * 60 * 1000L // 3 horas

        fun isSessionExpired(loginTimeMs: Long): Boolean {
            return System.currentTimeMillis() - loginTimeMs >= SESSION_DURATION_MS
        }
    }

    private val prefs: SharedPreferences = createPrefs(context)

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun createPrefs(context: Context): SharedPreferences {
        return try {
            createEncryptedPrefs(context)
        } catch (e: Exception) {
            try {
                context.deleteSharedPreferences(PREFS_NAME)
                createEncryptedPrefs(context)
            } catch (e2: Exception) {
                // Fallback to plain prefs if keystore is unavailable (e.g. emulator)
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveSession(token: String, uid: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_UID, uid)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUid(): String? = prefs.getString(KEY_UID, null)

    fun getLoginTime(): Long = prefs.getLong(KEY_LOGIN_TIME, 0L)

    fun isSessionValid(): Boolean {
        val token = getToken() ?: return false
        val loginTime = getLoginTime()
        return token.isNotEmpty() && !isSessionExpired(loginTime)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}