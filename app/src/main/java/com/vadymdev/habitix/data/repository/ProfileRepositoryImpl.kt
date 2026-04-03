package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.ProfilePreferencesDataSource
import com.vadymdev.habitix.domain.model.ProfileIdentity
import com.vadymdev.habitix.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(
    private val local: ProfilePreferencesDataSource
) : ProfileRepository {

    override fun observeProfileIdentity(): Flow<ProfileIdentity> = local.observeIdentity()

    override suspend fun getCurrentProfileIdentity(): ProfileIdentity = local.getCurrentIdentity()

    override suspend fun replaceProfileIdentity(displayName: String, bio: String) {
        local.replaceIdentity(displayName = displayName, bio = bio)
    }

    override suspend fun updateDisplayName(name: String) {
        local.updateDisplayName(name)
    }

    override suspend fun updateBio(bio: String) {
        local.updateBio(bio)
    }

    override suspend fun updateAvatarUri(uri: String?) {
        local.updateAvatarUri(uri)
    }
}
