package com.vadymdev.habitix.domain.repository

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getCurrentSettings(): AppSettings
    suspend fun replaceAll(settings: AppSettings)
    suspend fun resetToDefaults()
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentPalette(palette: AccentPalette)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setPushEnabled(enabled: Boolean)
    suspend fun setReminderTime(hour: Int, minute: Int)
    suspend fun setSoundsEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setAutoSyncEnabled(enabled: Boolean)
}
