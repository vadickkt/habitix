package com.vadymdev.habitix.domain.model

enum class ThemeMode {
    LIGHT,
    DARK
}

enum class AccentPalette {
    MINT,
    SKY,
    LAVENDER,
    PEACH,
    ROSE
}

enum class AppLanguage {
    UK,
    EN
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val accentPalette: AccentPalette = AccentPalette.MINT,
    val language: AppLanguage = AppLanguage.UK,
    val pushEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val soundsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val updatedAtMillis: Long = 0L
)
