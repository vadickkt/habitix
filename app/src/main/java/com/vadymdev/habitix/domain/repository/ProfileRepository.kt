package com.vadymdev.habitix.domain.repository

import com.vadymdev.habitix.domain.model.ProfileIdentity
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfileIdentity(): Flow<ProfileIdentity>
    suspend fun updateDisplayName(name: String)
    suspend fun updateBio(bio: String)
    suspend fun updateAvatarUri(uri: String?)
}
