package com.vadymdev.habitix.domain.repository

import com.vadymdev.habitix.domain.model.ProfileIdentity
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfileIdentity(): Flow<ProfileIdentity>
    suspend fun getCurrentProfileIdentity(): ProfileIdentity
    suspend fun replaceProfileIdentity(displayName: String, bio: String, updatedAtMillis: Long)
    suspend fun updateDisplayName(name: String)
    suspend fun updateBio(bio: String)
    suspend fun updateAvatarUri(uri: String?)
    suspend fun clearLocalData()
}
