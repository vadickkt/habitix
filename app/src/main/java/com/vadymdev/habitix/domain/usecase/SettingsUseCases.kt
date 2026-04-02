package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}

class SetThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: ThemeMode) = repository.setThemeMode(value)
}

class SetAccentPaletteUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: AccentPalette) = repository.setAccentPalette(value)
}

class SetLanguageUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: AppLanguage) = repository.setLanguage(value)
}

class SetPushEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) = repository.setPushEnabled(value)
}

class SetReminderTimeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(hour: Int, minute: Int) = repository.setReminderTime(hour, minute)
}

class SetSoundsEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) = repository.setSoundsEnabled(value)
}

class SetVibrationEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) = repository.setVibrationEnabled(value)
}

class SetBiometricEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) = repository.setBiometricEnabled(value)
}

class SetAutoSyncEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) = repository.setAutoSyncEnabled(value)
}

class GetCurrentSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): AppSettings = repository.getCurrentSettings()
}

class SyncSettingsUseCase(private val repository: SettingsSyncRepository) {
    suspend operator fun invoke(userId: String) = repository.sync(userId)
}
