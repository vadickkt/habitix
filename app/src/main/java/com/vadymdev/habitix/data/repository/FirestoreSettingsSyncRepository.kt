package com.vadymdev.habitix.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vadymdev.habitix.data.repository.sync.mapSyncThrowable
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
            firestore.collection("users")
                .document(userId)
                .collection("meta")
                .document("settings")
                .delete()
                .await()
        }.getOrElse { throw mapSyncThrowable(SyncTarget.SETTINGS, it) }
    }

    override suspend fun sync(userId: String) {
        runCatching {
            syncMutex.withLock {
                val local = settingsRepository.getCurrentSettings()
                val docRef = firestore.collection("users").document(userId).collection("meta").document("settings")
                val cloudDoc = docRef.get().await()

                if (!cloudDoc.exists()) {
                    upload(docRef = docRef.path, settings = local)
                    return
                }

                val remote = cloudDoc.toSettings()
                if (remote.updatedAtMillis > local.updatedAtMillis) {
                    settingsRepository.replaceAll(remote)
                } else {
                    upload(docRef.path, local)
                }
            }
        }.getOrElse { throw mapSyncThrowable(SyncTarget.SETTINGS, it) }
    }

    private suspend fun upload(docRef: String, settings: AppSettings) {
        firestore.document(docRef).set(
            mapOf(
                "themeMode" to settings.themeMode.name,
                "accentPalette" to settings.accentPalette.name,
                "language" to settings.language.name,
                "pushEnabled" to settings.pushEnabled,
                "reminderHour" to settings.reminderHour,
                "reminderMinute" to settings.reminderMinute,
                "soundsEnabled" to settings.soundsEnabled,
                "vibrationEnabled" to settings.vibrationEnabled,
                "biometricEnabled" to settings.biometricEnabled,
                "autoSyncEnabled" to settings.autoSyncEnabled,
                "updatedAtMillis" to settings.updatedAtMillis
            )
        ).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSettings(): AppSettings {
        return AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(getString("themeMode") ?: ThemeMode.LIGHT.name) }.getOrDefault(ThemeMode.LIGHT),
            accentPalette = runCatching { AccentPalette.valueOf(getString("accentPalette") ?: AccentPalette.MINT.name) }.getOrDefault(AccentPalette.MINT),
            language = runCatching { AppLanguage.valueOf(getString("language") ?: AppLanguage.UK.name) }.getOrDefault(AppLanguage.UK),
            pushEnabled = getBoolean("pushEnabled") ?: true,
            reminderHour = (getLong("reminderHour") ?: 9L).toInt(),
            reminderMinute = (getLong("reminderMinute") ?: 0L).toInt(),
            soundsEnabled = getBoolean("soundsEnabled") ?: true,
            vibrationEnabled = getBoolean("vibrationEnabled") ?: true,
            biometricEnabled = getBoolean("biometricEnabled") ?: false,
            autoSyncEnabled = getBoolean("autoSyncEnabled") ?: true,
            updatedAtMillis = getLong("updatedAtMillis") ?: 0L
        )
    }
}
