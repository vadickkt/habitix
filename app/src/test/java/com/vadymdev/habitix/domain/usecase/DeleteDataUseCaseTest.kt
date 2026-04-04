package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import com.vadymdev.habitix.domain.model.ProfileIdentity
import com.vadymdev.habitix.domain.repository.AchievementSyncRepository
import com.vadymdev.habitix.domain.repository.HabitRepository
import com.vadymdev.habitix.domain.repository.HabitSyncRepository
import com.vadymdev.habitix.domain.repository.ProfileRepository
import com.vadymdev.habitix.domain.repository.ProfileSyncRepository
import com.vadymdev.habitix.domain.repository.SettingsRepository
import com.vadymdev.habitix.domain.repository.SettingsSyncRepository
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

class DeleteDataUseCaseTest {

    @Test
    fun nullUserId_clearsOnlyLocalData() = runBlocking {
        val calls = mutableListOf<String>()
        val useCase = buildUseCase(
            calls = calls
        )

        useCase(userId = null)

        assertEquals(listOf("local-habits", "local-profile", "local-settings"), calls)
    }

    @Test
    fun nonBlankUserId_clearsLocalAndRemoteData() = runBlocking {
        val calls = mutableListOf<String>()
        val useCase = buildUseCase(
            calls = calls
        )

        useCase(userId = "uid")

        assertEquals(
            listOf(
                "local-habits",
                "local-profile",
                "local-settings",
                "remote-habits",
                "remote-profile",
                "remote-settings",
                "remote-achievements"
            ),
            calls
        )
    }

    @Test
    fun blankUserId_treatedAsNoUser_remoteSkipped() = runBlocking {
        val calls = mutableListOf<String>()
        val useCase = buildUseCase(calls = calls)

        useCase(userId = "  ")

        assertEquals(listOf("local-habits", "local-profile", "local-settings"), calls)
    }

    @Test
    fun localFailure_withNullUser_throwsLocalError() = runBlocking {
        val calls = mutableListOf<String>()
        val localError = IllegalStateException("local-failed")
        val useCase = buildUseCase(
            calls = calls,
            onLocalDeleteHabits = {
                calls.add("local-habits")
                throw localError
            }
        )

        try {
            useCase(userId = null)
            fail("Expected local error")
        } catch (t: Throwable) {
            assertEquals(localError, t)
            assertEquals(listOf("local-habits"), calls)
        }
    }

    @Test
    fun localFailure_withUser_stillRunsRemote_andThrowsFirstLocalError() = runBlocking {
        val calls = mutableListOf<String>()
        val localError = IllegalStateException("local-failed")
        val useCase = buildUseCase(
            calls = calls,
            onLocalDeleteHabits = {
                calls.add("local-habits")
                throw localError
            }
        )

        try {
            useCase(userId = "uid")
            fail("Expected local error")
        } catch (t: Throwable) {
            assertEquals(localError, t)
            assertTrue(calls.contains("remote-habits"))
            assertTrue(calls.contains("remote-profile"))
            assertTrue(calls.contains("remote-settings"))
            assertTrue(calls.contains("remote-achievements"))
        }
    }

    @Test
    fun remoteFailure_afterLocalSuccess_throwsRemoteError() = runBlocking {
        val calls = mutableListOf<String>()
        val remoteError = IllegalArgumentException("remote-failed")
        val useCase = buildUseCase(
            calls = calls,
            onRemoteClearHabits = {
                calls.add("remote-habits")
                throw remoteError
            }
        )

        try {
            useCase(userId = "uid")
            fail("Expected remote error")
        } catch (t: Throwable) {
            assertEquals(remoteError, t)
            assertEquals(
                listOf(
                    "local-habits",
                    "local-profile",
                    "local-settings",
                    "remote-habits"
                ),
                calls
            )
        }
    }

    @Test
    fun localAndRemoteFailures_throwFirstLocalError() = runBlocking {
        val calls = mutableListOf<String>()
        val localError = IllegalStateException("local-failed")
        val useCase = buildUseCase(
            calls = calls,
            onLocalDeleteHabits = {
                calls.add("local-habits")
                throw localError
            },
            onRemoteClearHabits = {
                calls.add("remote-habits")
                throw IllegalArgumentException("remote-failed")
            }
        )

        try {
            useCase(userId = "uid")
            fail("Expected local error")
        } catch (t: Throwable) {
            assertEquals(localError, t)
        }
    }

    private fun buildUseCase(
        calls: MutableList<String>,
        onLocalDeleteHabits: suspend () -> Unit = { calls.add("local-habits") },
        onLocalClearProfile: suspend () -> Unit = { calls.add("local-profile") },
        onLocalResetSettings: suspend () -> Unit = { calls.add("local-settings") },
        onRemoteClearHabits: suspend () -> Unit = { calls.add("remote-habits") },
        onRemoteClearProfile: suspend () -> Unit = { calls.add("remote-profile") },
        onRemoteClearSettings: suspend () -> Unit = { calls.add("remote-settings") },
        onRemoteClearAchievements: suspend () -> Unit = { calls.add("remote-achievements") }
    ): DeleteDataUseCase {
        val habitRepository = object : HabitRepository {
            override fun observeHabitsForDate(date: LocalDate) = emptyFlow<List<Habit>>()
            override fun observeStats(periodDays: Int) = emptyFlow<HabitStatsSnapshot>()
            override fun observeProfileAnalytics() = emptyFlow<ProfileAnalytics>()
            override suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate, completed: Boolean) = Unit
            override suspend fun updateHabit(habitId: Long, draft: HabitCreateDraft) = Unit
            override suspend fun hideHabitForDate(habitId: Long, date: LocalDate) = Unit
            override suspend fun deactivateHabitFromDate(habitId: Long, date: LocalDate) = Unit
            override suspend fun deleteAllHabits() = onLocalDeleteHabits()
            override suspend fun createHabit(draft: HabitCreateDraft) = Unit
            override suspend fun seedOnboardingHabits(habitKeys: Set<String>) = Unit
            override suspend fun getIncompleteHabitsForDate(date: LocalDate): List<Habit> = emptyList()
        }

        val profileRepository = object : ProfileRepository {
            override fun observeProfileIdentity() =
                flowOf(ProfileIdentity("", "", "", avatarUri = null, updatedAtMillis = 0L))

            override suspend fun getCurrentProfileIdentity(): ProfileIdentity =
                ProfileIdentity("", "", "", avatarUri = null, updatedAtMillis = 0L)

            override suspend fun replaceProfileIdentity(displayName: String, bio: String, updatedAtMillis: Long) = Unit
            override suspend fun updateDisplayName(name: String) = Unit
            override suspend fun updateBio(bio: String) = Unit
            override suspend fun updateAvatarUri(uri: String?) = Unit
            override suspend fun clearLocalData() = onLocalClearProfile()
        }

        val settingsRepository = object : SettingsRepository {
            override fun observeSettings() = flowOf(AppSettings())
            override suspend fun getCurrentSettings(): AppSettings = AppSettings()
            override suspend fun replaceAll(settings: AppSettings) = Unit
            override suspend fun resetToDefaults() = onLocalResetSettings()
            override suspend fun setThemeMode(mode: com.vadymdev.habitix.domain.model.ThemeMode) = Unit
            override suspend fun setAccentPalette(palette: com.vadymdev.habitix.domain.model.AccentPalette) = Unit
            override suspend fun setLanguage(language: com.vadymdev.habitix.domain.model.AppLanguage) = Unit
            override suspend fun setPushEnabled(enabled: Boolean) = Unit
            override suspend fun setReminderTime(hour: Int, minute: Int) = Unit
            override suspend fun setSoundsEnabled(enabled: Boolean) = Unit
            override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
            override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
            override suspend fun setAutoSyncEnabled(enabled: Boolean) = Unit
        }

        val habitSyncRepository = object : HabitSyncRepository {
            override suspend fun syncUserHabits(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = onRemoteClearHabits()
        }

        val profileSyncRepository = object : ProfileSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = onRemoteClearProfile()
        }

        val settingsSyncRepository = object : SettingsSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = onRemoteClearSettings()
        }

        val achievementSyncRepository = object : AchievementSyncRepository {
            override suspend fun sync(userId: String) = Unit
            override suspend fun clearUserData(userId: String) = onRemoteClearAchievements()
        }

        val deleteAllHabitsUseCase = DeleteAllHabitsUseCase(habitRepository)

        return DeleteDataUseCase(
            deleteAllHabitsUseCase = deleteAllHabitsUseCase,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            habitSyncRepository = habitSyncRepository,
            profileSyncRepository = profileSyncRepository,
            settingsSyncRepository = settingsSyncRepository,
            achievementSyncRepository = achievementSyncRepository
        )
    }
}
