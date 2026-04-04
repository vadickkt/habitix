package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.room.AchievementUnlockDao
import com.vadymdev.habitix.data.local.room.HabitCompletionDao
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitDao
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayDao
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import com.vadymdev.habitix.data.repository.habit.HabitInsightsCalculator
import com.vadymdev.habitix.domain.model.DUPLICATE_ACTIVE_HABIT_ERROR
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import com.vadymdev.habitix.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.util.UUID

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val hiddenDayDao: HiddenHabitDayDao,
    private val achievementUnlockDao: AchievementUnlockDao,
    private val insightsCalculator: HabitInsightsCalculator = HabitInsightsCalculator()
) : HabitRepository {

    override fun observeProfileAnalytics(): Flow<ProfileAnalytics> {
        return combine(
            habitDao.observeAllHabits(),
            completionDao.observeAllCompletions(),
            achievementUnlockDao.observeAllUnlocks()
        ) { habits, completions, unlocks ->
            insightsCalculator.buildProfileAnalytics(
                habits = habits,
                completions = completions,
                persistedUnlocks = unlocks
            )
        }
    }

    override fun observeStats(periodDays: Int): Flow<HabitStatsSnapshot> {
        return combine(
            habitDao.observeAllHabits(),
            completionDao.observeAllCompletions()
        ) { habits, completions ->
            insightsCalculator.buildStatsSnapshot(
                habits = habits,
                completions = completions,
                periodDays = periodDays
            )
        }
    }

    override fun observeHabitsForDate(date: LocalDate): Flow<List<Habit>> {
        val epochDay = date.toEpochDay()
        return combine(
            habitDao.observeActiveHabits(),
            completionDao.observeCompletionsForDate(epochDay),
            hiddenDayDao.observeHiddenForDate(epochDay),
            completionDao.observeCompletionCounts()
        ) { habits, completions, hiddenDays, completionCounts ->
            val completionIds = completions.map { it.habitId }.toSet()
            val hiddenIds = hiddenDays.map { it.habitId }.toSet()
            val countsByHabit = completionCounts.associate { it.habitId to it.completionCount }

            habits
                .filter { insightsCalculator.matches(it, date) }
                .filterNot { hiddenIds.contains(it.id) }
                .map { entity ->
                    Habit(
                        id = entity.id,
                        title = entity.title,
                        iconKey = entity.iconKey,
                        colorKey = entity.colorKey,
                        frequencyType = HabitFrequencyType.valueOf(entity.frequencyType),
                        customDays = insightsCalculator.parseDays(entity.customDaysCsv),
                        reminderEnabled = entity.reminderEnabled,
                        isCompletedForSelectedDate = completionIds.contains(entity.id),
                        streakDays = countsByHabit[entity.id] ?: 0
                    )
                }
        }
    }

    override suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate, completed: Boolean) {
        val epochDay = date.toEpochDay()
        if (completed) {
            completionDao.upsertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    dateEpochDay = epochDay,
                    completedAtMillis = System.currentTimeMillis()
                )
            )
        } else {
            completionDao.removeCompletion(habitId, epochDay)
        }
        refreshAchievementUnlockLog()
    }

    override suspend fun updateHabit(habitId: Long, draft: HabitCreateDraft) {
        val customDaysCsv = draft.customDays
            .sortedBy { it.value }
            .joinToString(",") { it.value.toString() }

        habitDao.updateHabit(
            id = habitId,
            title = draft.title.trim(),
            iconKey = draft.iconKey,
            colorKey = draft.colorKey,
            frequencyType = draft.frequencyType.name,
            customDaysCsv = customDaysCsv,
            reminderEnabled = draft.reminderEnabled
        )
        refreshAchievementUnlockLog()
    }

    override suspend fun hideHabitForDate(habitId: Long, date: LocalDate) {
        val epochDay = date.toEpochDay()
        completionDao.removeCompletion(habitId, epochDay)
        hiddenDayDao.upsert(HiddenHabitDayEntity(habitId = habitId, dateEpochDay = epochDay))
        refreshAchievementUnlockLog()
    }

    override suspend fun deactivateHabitFromDate(habitId: Long, date: LocalDate) {
        val epochDay = date.toEpochDay()
        completionDao.removeCompletion(habitId, epochDay)
        habitDao.updateActiveUntil(habitId, epochDay - 1)
        refreshAchievementUnlockLog()
    }

    override suspend fun deleteAllHabits() {
        completionDao.removeAll()
        hiddenDayDao.deleteAll()
        habitDao.deleteAllHabits()
        achievementUnlockDao.deleteAll()
    }

    override suspend fun createHabit(draft: HabitCreateDraft) {
        val today = LocalDate.now()
        val normalizedTitle = normalizeTitle(draft.title)
        val duplicateExists = habitDao.getActiveHabits().any { existing ->
            insightsCalculator.matches(existing, today) && normalizeTitle(existing.title) == normalizedTitle
        }
        if (duplicateExists) {
            throw IllegalArgumentException(DUPLICATE_ACTIVE_HABIT_ERROR)
        }

        val customDaysCsv = draft.customDays
            .sortedBy { it.value }
            .joinToString(",") { it.value.toString() }

        habitDao.insertHabit(
            HabitEntity(
                cloudId = UUID.randomUUID().toString(),
                title = draft.title.trim(),
                iconKey = draft.iconKey,
                colorKey = draft.colorKey,
                frequencyType = draft.frequencyType.name,
                customDaysCsv = customDaysCsv,
                reminderEnabled = draft.reminderEnabled,
                reminderHour = 20,
                reminderMinute = 0,
                createdAt = System.currentTimeMillis(),
                startEpochDay = today.toEpochDay(),
                activeUntilEpochDay = null,
                isArchived = false,
                source = "manual"
            )
        )
        refreshAchievementUnlockLog()
    }

    override suspend fun seedOnboardingHabits(habitKeys: Set<String>) {
        val defaults = onboardingDefaults().filter { habitKeys.contains(it.key) }
        if (defaults.isEmpty()) return

        val entities = defaults.mapNotNull { model ->
            val exists = habitDao.countOnboardingHabitByTitle(model.title) > 0
            if (exists) {
                null
            } else {
                HabitEntity(
                    cloudId = UUID.randomUUID().toString(),
                    title = model.title,
                    iconKey = model.iconKey,
                    colorKey = model.colorKey,
                    frequencyType = HabitFrequencyType.DAILY.name,
                    customDaysCsv = "",
                    reminderEnabled = true,
                    reminderHour = 20,
                    reminderMinute = 0,
                    createdAt = System.currentTimeMillis(),
                    startEpochDay = LocalDate.now().toEpochDay(),
                    activeUntilEpochDay = null,
                    isArchived = false,
                    source = "onboarding"
                )
            }
        }

        if (entities.isNotEmpty()) {
            habitDao.insertHabits(entities)
            refreshAchievementUnlockLog()
        }
    }

    override suspend fun getIncompleteHabitsForDate(date: LocalDate): List<Habit> {
        val habits = habitDao.getActiveHabits().filter { insightsCalculator.matches(it, date) }
        val completionIds = completionDao.getCompletionsForDate(date.toEpochDay()).map { it.habitId }.toSet()
        val completionCounts = completionDao.getCompletionCounts().associate { it.habitId to it.completionCount }
        val hiddenIds = hiddenDayDao.getAllHiddenDays().asSequence()
            .filter { it.dateEpochDay == date.toEpochDay() }
            .map { it.habitId }
            .toSet()

        return habits
            .filterNot { completionIds.contains(it.id) }
            .filterNot { hiddenIds.contains(it.id) }
            .map { entity ->
                Habit(
                    id = entity.id,
                    title = entity.title,
                    iconKey = entity.iconKey,
                    colorKey = entity.colorKey,
                    frequencyType = HabitFrequencyType.valueOf(entity.frequencyType),
                    customDays = insightsCalculator.parseDays(entity.customDaysCsv),
                    reminderEnabled = entity.reminderEnabled,
                    isCompletedForSelectedDate = false,
                    streakDays = completionCounts[entity.id] ?: 0
                )
            }
    }

    private fun normalizeTitle(value: String): String {
        return value
            .trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
    }

    private fun onboardingDefaults(): List<OnboardingHabitDefault> {
        return listOf(
            OnboardingHabitDefault("water", "Випити 8 склянок води", "water", "blue"),
            OnboardingHabitDefault("meditation", "Медитація 10 хв", "mind", "pink"),
            OnboardingHabitDefault("morning", "Тренування", "fitness", "mint"),
            OnboardingHabitDefault("reading", "Читати 20 хвилин", "book", "orange"),
            OnboardingHabitDefault("sleep", "Сон до 23:00", "moon", "purple"),
            OnboardingHabitDefault("gratitude", "Вдячність", "heart", "pink")
        )
    }

    private suspend fun refreshAchievementUnlockLog() {
        val habits = habitDao.getAllHabits()
        val completions = completionDao.getAllCompletions()
        val unlocked = insightsCalculator.computeUnlockedAchievements(
            habits = habits,
            completions = completions,
            today = LocalDate.now()
        )

        unlocked.forEach { achievementUnlockDao.insertIgnore(it) }
    }
}

data class OnboardingHabitDefault(
    val key: String,
    val title: String,
    val iconKey: String,
    val colorKey: String
)
