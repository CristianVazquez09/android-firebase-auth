package com.wolfpack.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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