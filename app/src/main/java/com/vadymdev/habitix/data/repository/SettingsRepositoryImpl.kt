package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val local: SettingsPreferencesDataSource
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = local.observeSettings()

    override suspend fun getCurrentSettings(): AppSettings = local.getCurrentSettings()

    override suspend fun replaceAll(settings: AppSettings) = local.replaceAll(settings)

    override suspend fun resetToDefaults() = local.clearAllLocalData()

    override suspend fun setThemeMode(mode: ThemeMode) = local.setThemeMode(mode)
    override suspend fun setAccentPalette(palette: AccentPalette) = local.setAccentPalette(palette)
    override suspend fun setLanguage(language: AppLanguage) = local.setLanguage(language)
    override suspend fun setPushEnabled(enabled: Boolean) = local.setPushEnabled(enabled)
    override suspend fun setReminderTime(hour: Int, minute: Int) = local.setReminderTime(hour, minute)
    override suspend fun setSoundsEnabled(enabled: Boolean) = local.setSoundsEnabled(enabled)
    override suspend fun setVibrationEnabled(enabled: Boolean) = local.setVibrationEnabled(enabled)
    override suspend fun setBiometricEnabled(enabled: Boolean) = local.setBiometricEnabled(enabled)
    override suspend fun setAutoSyncEnabled(enabled: Boolean) = local.setAutoSyncEnabled(enabled)
}
