package com.vadymdev.habitix.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.authPrefsDataStore by preferencesDataStore(name = "habitix_auth")

class AuthPreferencesDataSource(private val context: Context) {

    private val authMethodKey = stringPreferencesKey("auth_method")

    suspend fun markGoogleAuth() {
        context.authPrefsDataStore.edit { prefs ->
            prefs[authMethodKey] = "google"
        }
    }

    suspend fun markGuestAuth() {
        context.authPrefsDataStore.edit { prefs ->
            prefs[authMethodKey] = "guest"
        }
    }
}
