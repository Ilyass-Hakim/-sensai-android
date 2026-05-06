package com.example.sensai.data.local

import android.content.Context
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
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }

    /** Save access + refresh tokens and username after login/register */
    suspend fun saveTokens(accessToken: String, refreshToken: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY]  = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USERNAME_KEY]      = username
        }
    }

    /** Clear all auth data on logout */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // --- Legacy compat (used in old code, kept to avoid compile errors) ---
    @Deprecated("Use saveTokens()", replaceWith = ReplaceWith("saveTokens(token, \"\", \"\")"))
    suspend fun saveToken(token: String) = saveTokens(token, "", "")

    @Deprecated("Use clearAll()")
    suspend fun deleteToken() = clearAll()
}

