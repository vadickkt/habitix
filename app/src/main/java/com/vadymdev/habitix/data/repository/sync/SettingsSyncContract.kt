package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.repository.SettingsRepository
import kotlinx.coroutines.tasks.await
import java.util.Locale

private fun systemDefaultLanguage(): AppLanguage {
    return if (Locale.getDefault().language.equals("uk", ignoreCase = true)) AppLanguage.UK else AppLanguage.EN
}

internal data class SettingsCloudRecord(
    val themeMode: String,
    val accentPalette: String,
    val language: String,
    val pushEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val soundsEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val biometricEnabled: Boolean,
    val autoSyncEnabled: Boolean,
    val updatedAtMillis: Long
)

internal interface SettingsCloudStore {
    suspend fun get(userId: String): SettingsCloudRecord?
    suspend fun set(userId: String, value: SettingsCloudRecord)
    suspend fun clear(userId: String)
}

internal class FirestoreSettingsCloudStore(
    private val firestore: FirebaseFirestore
) : SettingsCloudStore {
    override suspend fun get(userId: String): SettingsCloudRecord? {
        val doc = firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("settings")
            .get()
            .await()

        if (!doc.exists()) return null

        return SettingsCloudRecord(
            themeMode = doc.getString("themeMode") ?: ThemeMode.LIGHT.name,
            accentPalette = doc.getString("accentPalette") ?: AccentPalette.MINT.name,
            language = doc.getString("language") ?: systemDefaultLanguage().name,
            pushEnabled = doc.getBoolean("pushEnabled") ?: true,
            reminderHour = (doc.getLong("reminderHour") ?: 9L).toInt(),
            reminderMinute = (doc.getLong("reminderMinute") ?: 0L).toInt(),
            soundsEnabled = doc.getBoolean("soundsEnabled") ?: true,
            vibrationEnabled = doc.getBoolean("vibrationEnabled") ?: true,
            biometricEnabled = doc.getBoolean("biometricEnabled") ?: false,
            autoSyncEnabled = doc.getBoolean("autoSyncEnabled") ?: true,
            updatedAtMillis = doc.getLong("updatedAtMillis") ?: 0L
        )
    }

    override suspend fun set(userId: String, value: SettingsCloudRecord) {
        firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("settings")
            .set(
                mapOf(
                    "themeMode" to value.themeMode,
                    "accentPalette" to value.accentPalette,
                    "language" to value.language,
                    "pushEnabled" to value.pushEnabled,
                    "reminderHour" to value.reminderHour,
                    "reminderMinute" to value.reminderMinute,
                    "soundsEnabled" to value.soundsEnabled,
                    "vibrationEnabled" to value.vibrationEnabled,
                    "biometricEnabled" to value.biometricEnabled,
                    "autoSyncEnabled" to value.autoSyncEnabled,
                    "updatedAtMillis" to value.updatedAtMillis
                )
            )
            .await()
    }

    override suspend fun clear(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("meta")
            .document("settings")
            .delete()
            .await()
    }
}

internal class SettingsSyncContract(
    private val settingsRepository: SettingsRepository,
    private val cloudStore: SettingsCloudStore
) {
    suspend fun clearUserData(userId: String) {
        cloudStore.clear(userId)
    }

    suspend fun sync(userId: String) {
        val local = settingsRepository.getCurrentSettings()
        val remote = cloudStore.get(userId)

        if (remote == null) {
            cloudStore.set(userId, local.toCloudRecord())
            return
        }

        if (remote.updatedAtMillis > local.updatedAtMillis) {
            settingsRepository.replaceAll(remote.toDomain())
        } else {
            cloudStore.set(userId, local.toCloudRecord())
        }
    }

    private fun AppSettings.toCloudRecord(): SettingsCloudRecord {
        return SettingsCloudRecord(
            themeMode = themeMode.name,
            accentPalette = accentPalette.name,
            language = language.name,
            pushEnabled = pushEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            soundsEnabled = soundsEnabled,
            vibrationEnabled = vibrationEnabled,
            biometricEnabled = biometricEnabled,
            autoSyncEnabled = autoSyncEnabled,
            updatedAtMillis = updatedAtMillis
        )
    }

    private fun SettingsCloudRecord.toDomain(): AppSettings {
        return AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.LIGHT),
            accentPalette = runCatching { AccentPalette.valueOf(accentPalette) }.getOrDefault(AccentPalette.MINT),
            language = runCatching { AppLanguage.valueOf(language) }.getOrDefault(systemDefaultLanguage()),
            pushEnabled = pushEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            soundsEnabled = soundsEnabled,
            vibrationEnabled = vibrationEnabled,
            biometricEnabled = biometricEnabled,
            autoSyncEnabled = autoSyncEnabled,
            updatedAtMillis = updatedAtMillis
        )
    }
}
