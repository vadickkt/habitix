package com.vadymdev.habitix.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
    private val updatedAtKey = longPreferencesKey("profile_updated_at")

    fun observeIdentity(): Flow<ProfileIdentity> {
        return context.profilePrefsDataStore.data.map { prefs ->
            val displayName = prefs[nameKey] ?: "Користувач"
            val bio = prefs[bioKey] ?: "Будую кращу версію себе"
            val storedAvatarUri = prefs[avatarUriKey]
            val resolvedAvatarUri = storedAvatarUri?.takeIf { isAvatarUriAvailable(it) }
            ProfileIdentity(
                displayName = displayName,
                bio = bio,
                avatarInitials = initialsFor(displayName),
                avatarUri = resolvedAvatarUri,
                updatedAtMillis = prefs[updatedAtKey] ?: 0L
            )
        }
    }

    suspend fun getCurrentIdentity(): ProfileIdentity {
        return observeIdentity().first()
    }

    suspend fun replaceIdentity(displayName: String, bio: String, updatedAtMillis: Long = System.currentTimeMillis()) {
        context.profilePrefsDataStore.edit { prefs ->
            prefs[nameKey] = displayName.trim().ifBlank { "Користувач" }
            prefs[bioKey] = bio.trim().ifBlank { "Будую кращу версію себе" }
            prefs[updatedAtKey] = updatedAtMillis
        }
    }

    suspend fun updateDisplayName(name: String) {
        val trimmed = name.trim().ifBlank { "Користувач" }
        context.profilePrefsDataStore.edit { prefs ->
            prefs[nameKey] = trimmed
            prefs[updatedAtKey] = System.currentTimeMillis()
        }
    }

    suspend fun updateBio(bio: String) {
        val trimmed = bio.trim().ifBlank { "Будую кращу версію себе" }
        context.profilePrefsDataStore.edit { prefs ->
            prefs[bioKey] = trimmed
            prefs[updatedAtKey] = System.currentTimeMillis()
        }
    }

    suspend fun updateAvatarUri(uri: String?) {
        context.profilePrefsDataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(avatarUriKey)
            } else {
                prefs[avatarUriKey] = uri
            }
            prefs[updatedAtKey] = System.currentTimeMillis()
        }
    }

    suspend fun clearLocalData() {
        context.profilePrefsDataStore.edit { prefs ->
            prefs[nameKey] = "Користувач"
            prefs[bioKey] = "Будую кращу версію себе"
            prefs.remove(avatarUriKey)
            prefs.remove(updatedAtKey)
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

    private fun isAvatarUriAvailable(value: String): Boolean {
        if (
            value.startsWith("content://") ||
            value.startsWith("file://") ||
            value.startsWith("http://") ||
            value.startsWith("https://") ||
            value.startsWith("android.resource://")
        ) {
            return true
        }

        val local = java.io.File(value)
        return local.exists() && local.isFile
    }
}
