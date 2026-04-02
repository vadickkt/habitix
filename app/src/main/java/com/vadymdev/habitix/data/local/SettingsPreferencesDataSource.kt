package com.vadymdev.habitix.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "habitix_settings")

class SettingsPreferencesDataSource(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val paletteKey = stringPreferencesKey("accent_palette")
    private val languageKey = stringPreferencesKey("language")
    private val pushKey = booleanPreferencesKey("push_enabled")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")
    private val soundsKey = booleanPreferencesKey("sounds_enabled")
    private val vibrationKey = booleanPreferencesKey("vibration_enabled")
    private val biometricKey = booleanPreferencesKey("biometric_enabled")
    private val autoSyncKey = booleanPreferencesKey("auto_sync_enabled")
    private val updatedAtKey = longPreferencesKey("settings_updated_at")

    fun observeSettings(): Flow<AppSettings> = context.settingsDataStore.data.map { prefs -> prefs.toAppSettings() }

    suspend fun getCurrentSettings(): AppSettings = context.settingsDataStore.data.first().toAppSettings()

    suspend fun replaceAll(settings: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeKey] = settings.themeMode.name
            prefs[paletteKey] = settings.accentPalette.name
            prefs[languageKey] = settings.language.name
            prefs[pushKey] = settings.pushEnabled
            prefs[reminderHourKey] = settings.reminderHour
            prefs[reminderMinuteKey] = settings.reminderMinute
            prefs[soundsKey] = settings.soundsEnabled
            prefs[vibrationKey] = settings.vibrationEnabled
            prefs[biometricKey] = settings.biometricEnabled
            prefs[autoSyncKey] = settings.autoSyncEnabled
            prefs[updatedAtKey] = settings.updatedAtMillis
        }
    }

    suspend fun setThemeMode(value: ThemeMode) = update { prefs -> prefs[themeKey] = value.name }
    suspend fun setAccentPalette(value: AccentPalette) = update { prefs -> prefs[paletteKey] = value.name }
    suspend fun setLanguage(value: AppLanguage) = update { prefs -> prefs[languageKey] = value.name }
    suspend fun setPushEnabled(value: Boolean) = update { prefs -> prefs[pushKey] = value }

    suspend fun setReminderTime(hour: Int, minute: Int) = update {
        it[reminderHourKey] = hour
        it[reminderMinuteKey] = minute
    }

    suspend fun setSoundsEnabled(value: Boolean) = update { prefs -> prefs[soundsKey] = value }
    suspend fun setVibrationEnabled(value: Boolean) = update { prefs -> prefs[vibrationKey] = value }
    suspend fun setBiometricEnabled(value: Boolean) = update { prefs -> prefs[biometricKey] = value }
    suspend fun setAutoSyncEnabled(value: Boolean) = update { prefs -> prefs[autoSyncKey] = value }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        context.settingsDataStore.edit { prefs ->
            block(prefs)
            prefs[updatedAtKey] = System.currentTimeMillis()
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(this[themeKey] ?: ThemeMode.LIGHT.name) }.getOrDefault(ThemeMode.LIGHT),
            accentPalette = runCatching { AccentPalette.valueOf(this[paletteKey] ?: AccentPalette.MINT.name) }.getOrDefault(AccentPalette.MINT),
            language = runCatching { AppLanguage.valueOf(this[languageKey] ?: AppLanguage.UK.name) }.getOrDefault(AppLanguage.UK),
            pushEnabled = this[pushKey] ?: true,
            reminderHour = this[reminderHourKey] ?: 9,
            reminderMinute = this[reminderMinuteKey] ?: 0,
            soundsEnabled = this[soundsKey] ?: true,
            vibrationEnabled = this[vibrationKey] ?: true,
            biometricEnabled = this[biometricKey] ?: false,
            autoSyncEnabled = this[autoSyncKey] ?: true,
            updatedAtMillis = this[updatedAtKey] ?: 0L
        )
    }
}
