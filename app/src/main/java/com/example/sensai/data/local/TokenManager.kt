package com.example.sensai.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY  = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USERNAME_KEY      = stringPreferencesKey("username")
        private val USER_ID_KEY       = stringPreferencesKey("user_id")
        private val IS_VOICE_ENABLED_KEY = booleanPreferencesKey("is_voice_enabled")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[USER_ID_KEY]?.toLongOrNull() }
    val isVoiceEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_VOICE_ENABLED_KEY] ?: true }

    /** Save access + refresh tokens and username after login/register */
    suspend fun saveTokens(accessToken: String, refreshToken: String, username: String, userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY]  = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USERNAME_KEY]      = username
            prefs[USER_ID_KEY]       = userId.toString()
        }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_VOICE_ENABLED_KEY] = enabled }
    }

    /** Clear all auth data on logout */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // --- Legacy compat (used in old code, kept to avoid compile errors) ---
    @Deprecated("Use saveTokens()", replaceWith = ReplaceWith("saveTokens(token, \"\", \"\", -1)"))
    suspend fun saveToken(token: String) = saveTokens(token, "", "", -1)

    @Deprecated("Use clearAll()")
    suspend fun deleteToken() = clearAll()
}

