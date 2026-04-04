package com.vadymdev.habitix.data.repository.sync

import com.vadymdev.habitix.domain.model.ProfileIdentity
import com.vadymdev.habitix.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSyncContractTest {

    @Test
    fun remoteNewer_replacesLocalWithFallbackBioWhenBlank() = runBlocking {
        val localRepo = FakeProfileRepository(
            ProfileIdentity("Local", "Local bio", "L", null, updatedAtMillis = 10L)
        )
        val cloudStore = FakeProfileCloudStore(
            remote = ProfileCloudRecord("Remote", "", updatedAtMillis = 20L)
        )

        ProfileSyncContract(localRepo, cloudStore).sync("uid")

        assertEquals("Remote", localRepo.current.displayName)
        assertEquals("Будую кращу версію себе", localRepo.current.bio)
    }

    @Test
    fun localNewer_uploadsLocalToCloud() = runBlocking {
        val localRepo = FakeProfileRepository(
            ProfileIdentity("Me", "Bio", "M", null, updatedAtMillis = 30L)
        )
        val cloudStore = FakeProfileCloudStore(
            remote = ProfileCloudRecord("Old", "Old bio", updatedAtMillis = 10L)
        )

        ProfileSyncContract(localRepo, cloudStore).sync("uid")

        assertEquals("Me", cloudStore.lastSet?.displayName)
        assertEquals(30L, cloudStore.lastSet?.updatedAtMillis)
    }

    @Test
    fun clear_deletesRemoteRecord() = runBlocking {
        val localRepo = FakeProfileRepository(ProfileIdentity("A", "B", "AB", null, 1L))
        val cloudStore = FakeProfileCloudStore(null)

        ProfileSyncContract(localRepo, cloudStore).clearUserData("uid")

        assertTrue(cloudStore.cleared)
    }

    private class FakeProfileCloudStore(
        private var remote: ProfileCloudRecord?
    ) : ProfileCloudStore {
        var cleared = false
        var lastSet: ProfileCloudRecord? = null

        override suspend fun get(userId: String): ProfileCloudRecord? = remote

        override suspend fun set(userId: String, value: ProfileCloudRecord) {
            lastSet = value
            remote = value
        }

        override suspend fun clear(userId: String) {
            cleared = true
            remote = null
        }
    }

    private class FakeProfileRepository(initial: ProfileIdentity) : ProfileRepository {
        private val flow = MutableStateFlow(initial)
        val current: ProfileIdentity get() = flow.value

        override fun observeProfileIdentity(): Flow<ProfileIdentity> = flow
        override suspend fun getCurrentProfileIdentity(): ProfileIdentity = flow.value

        override suspend fun replaceProfileIdentity(displayName: String, bio: String, updatedAtMillis: Long) {
            flow.value = flow.value.copy(displayName = displayName, bio = bio, updatedAtMillis = updatedAtMillis)
        }

        override suspend fun updateDisplayName(name: String) {
            flow.value = flow.value.copy(displayName = name)
        }

        override suspend fun updateBio(bio: String) {
            flow.value = flow.value.copy(bio = bio)
        }

        override suspend fun updateAvatarUri(uri: String?) {
            flow.value = flow.value.copy(avatarUri = uri)
        }

        override suspend fun clearLocalData() = Unit
    }
}
