package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.repository.sync.mapSyncThrowable
import com.vadymdev.habitix.data.repository.sync.FirestoreSettingsCloudStore
import com.vadymdev.habitix.data.repository.sync.SettingsSyncContract
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FirestoreSettingsSyncRepository(
    private val firestore: FirebaseFirestore,
    private val settingsRepository: SettingsRepository
) : SettingsSyncRepository {

    companion object {
        private val syncMutex = Mutex()
    }

    override suspend fun clearUserData(userId: String) {
        runCatching {
            SettingsSyncContract(
                settingsRepository = settingsRepository,
                cloudStore = FirestoreSettingsCloudStore(firestore)
            ).clearUserData(userId)
        }.getOrElse { throw mapSyncThrowable(SyncTarget.SETTINGS, it) }
    }

    override suspend fun sync(userId: String) {
        runCatching {
            syncMutex.withLock {
                SettingsSyncContract(
                    settingsRepository = settingsRepository,
                    cloudStore = FirestoreSettingsCloudStore(firestore)
                ).sync(userId)
            }
        }.getOrElse { throw mapSyncThrowable(SyncTarget.SETTINGS, it) }
    }
}
