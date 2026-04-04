package com.vadymdev.habitix.data.repository.sync

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSyncContractTest {

    @Test
    fun guestLocalSettings_whenCloudEmpty_uploadsLocalWithoutOverwrite() = runBlocking {
        val local = AppSettings(
            themeMode = ThemeMode.DARK,
            accentPalette = AccentPalette.PEACH,
            language = AppLanguage.EN,
            pushEnabled = false,
            reminderHour = 6,
            reminderMinute = 45,
            updatedAtMillis = 44L
        )
        val localRepo = FakeSettingsRepository(local)
        val cloudStore = FakeSettingsCloudStore(remote = null)

        SettingsSyncContract(localRepo, cloudStore).sync("uid")

        assertEquals(local.updatedAtMillis, cloudStore.lastSet?.updatedAtMillis)
        assertEquals(local.themeMode.name, cloudStore.lastSet?.themeMode)
        assertEquals(local.accentPalette.name, cloudStore.lastSet?.accentPalette)
        assertEquals(local.language.name, cloudStore.lastSet?.language)
        assertEquals(local.updatedAtMillis, localRepo.current.updatedAtMillis)
    }

    @Test
    fun remoteNewer_replacesLocal() = runBlocking {
        val localRepo = FakeSettingsRepository(
            AppSettings(
                themeMode = ThemeMode.LIGHT,
                accentPalette = AccentPalette.MINT,
                language = AppLanguage.EN,
                updatedAtMillis = 10L
            )
        )
        val cloudStore = FakeSettingsCloudStore(
            remote = SettingsCloudRecord(
                themeMode = ThemeMode.DARK.name,
                accentPalette = AccentPalette.ROSE.name,
                language = AppLanguage.UK.name,
                pushEnabled = false,
                reminderHour = 8,
                reminderMinute = 30,
                soundsEnabled = false,
                vibrationEnabled = false,
                biometricEnabled = true,
                autoSyncEnabled = true,
                updatedAtMillis = 20L
            )
        )

        SettingsSyncContract(localRepo, cloudStore).sync("uid")

        assertEquals(ThemeMode.DARK, localRepo.current.themeMode)
        assertEquals(AccentPalette.ROSE, localRepo.current.accentPalette)
    }

    @Test
    fun localNewer_uploadsToCloud() = runBlocking {
        val local = AppSettings(updatedAtMillis = 20L, language = AppLanguage.EN)
        val localRepo = FakeSettingsRepository(local)
        val cloudStore = FakeSettingsCloudStore(
            remote = SettingsCloudRecord(
                themeMode = ThemeMode.DARK.name,
                accentPalette = AccentPalette.SKY.name,
                language = AppLanguage.UK.name,
                pushEnabled = true,
                reminderHour = 9,
                reminderMinute = 0,
                soundsEnabled = true,
                vibrationEnabled = true,
                biometricEnabled = false,
                autoSyncEnabled = true,
                updatedAtMillis = 10L
            )
        )

        SettingsSyncContract(localRepo, cloudStore).sync("uid")

        assertEquals(local.updatedAtMillis, cloudStore.lastSet?.updatedAtMillis)
        assertEquals(local.language.name, cloudStore.lastSet?.language)
    }

    @Test
    fun clear_deletesRemoteRecord() = runBlocking {
        val localRepo = FakeSettingsRepository(AppSettings())
        val cloudStore = FakeSettingsCloudStore(remote = null)

        SettingsSyncContract(localRepo, cloudStore).clearUserData("uid")

        assertTrue(cloudStore.cleared)
    }

    private class FakeSettingsCloudStore(
        private var remote: SettingsCloudRecord?
    ) : SettingsCloudStore {
        var lastSet: SettingsCloudRecord? = null
        var cleared: Boolean = false

        override suspend fun get(userId: String): SettingsCloudRecord? = remote

        override suspend fun set(userId: String, value: SettingsCloudRecord) {
            lastSet = value
            remote = value
        }

        override suspend fun clear(userId: String) {
            cleared = true
            remote = null
        }
    }

    private class FakeSettingsRepository(initial: AppSettings) : SettingsRepository {
        private val flow = MutableStateFlow(initial)
        val current: AppSettings get() = flow.value

        override fun observeSettings(): Flow<AppSettings> = flow
        override suspend fun getCurrentSettings(): AppSettings = flow.value
        override suspend fun replaceAll(settings: AppSettings) {
            flow.value = settings
        }

        override suspend fun resetToDefaults() = Unit
        override suspend fun setThemeMode(mode: ThemeMode) {
            flow.value = flow.value.copy(themeMode = mode)
        }

        override suspend fun setAccentPalette(palette: AccentPalette) {
            flow.value = flow.value.copy(accentPalette = palette)
        }

        override suspend fun setLanguage(language: AppLanguage) {
            flow.value = flow.value.copy(language = language)
        }

        override suspend fun setPushEnabled(enabled: Boolean) = Unit
        override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
        override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
        override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
    }
}
