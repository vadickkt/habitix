package com.vadymdev.habitix.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vadymdev.habitix.domain.model.ProfileIdentity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.profilePrefsDataStore by preferencesDataStore(name = "habitix_profile")

class ProfilePreferencesDataSource(private val context: Context) {

    private val nameKey = stringPreferencesKey("profile_name")
    private val bioKey = stringPreferencesKey("profile_bio")
    private val avatarUriKey = stringPreferencesKey("profile_avatar_uri")

    fun observeIdentity(): Flow<ProfileIdentity> {
        return context.profilePrefsDataStore.data.map { prefs ->
            val displayName = prefs[nameKey] ?: "Користувач"
            val bio = prefs[bioKey] ?: "Будую кращу версію себе"
            ProfileIdentity(
                displayName = displayName,
                bio = bio,
                avatarInitials = initialsFor(displayName),
                avatarUri = prefs[avatarUriKey]
            )
        }
    }

    suspend fun getCurrentIdentity(): ProfileIdentity {
        return observeIdentity().first()
    }

    suspend fun replaceIdentity(displayName: String, bio: String) {
        context.profilePrefsDataStore.edit { prefs ->
            prefs[nameKey] = displayName.trim().ifBlank { "Користувач" }
            prefs[bioKey] = bio.trim().ifBlank { "Будую кращу версію себе" }
        }
    }

    suspend fun updateDisplayName(name: String) {
        val trimmed = name.trim().ifBlank { "Користувач" }
        context.profilePrefsDataStore.edit { prefs ->
            prefs[nameKey] = trimmed
        }
    }

    suspend fun updateBio(bio: String) {
        val trimmed = bio.trim().ifBlank { "Будую кращу версію себе" }
        context.profilePrefsDataStore.edit { prefs ->
            prefs[bioKey] = trimmed
        }
    }

    suspend fun updateAvatarUri(uri: String?) {
        context.profilePrefsDataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(avatarUriKey)
            } else {
                prefs[avatarUriKey] = uri
            }
        }
    }

    suspend fun clearLocalData() {
        context.profilePrefsDataStore.edit { prefs ->
            prefs[nameKey] = "Користувач"
            prefs[bioKey] = "Будую кращу версію себе"
            prefs.remove(avatarUriKey)
        }

        val avatarsDir = java.io.File(context.filesDir, "avatars")
        if (avatarsDir.exists()) {
            avatarsDir.deleteRecursively()
        }

        val cameraCacheDir = java.io.File(context.cacheDir, "images")
        if (cameraCacheDir.exists()) {
            cameraCacheDir.deleteRecursively()
        }

        val shareCacheDir = java.io.File(context.cacheDir, "share")
        if (shareCacheDir.exists()) {
            shareCacheDir.deleteRecursively()
        }
    }

    private fun initialsFor(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "HU"
            parts.size == 1 -> parts.first().take(2).uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }
}
