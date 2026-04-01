package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.room.HabitCompletionDao
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitDao
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) : HabitRepository {

    override fun observeHabitsForDate(date: LocalDate): Flow<List<Habit>> {
        val epochDay = date.toEpochDay()
        return combine(
            habitDao.observeActiveHabits(),
            completionDao.observeCompletionsForDate(epochDay)
        ) { habits, completions ->
            val completionIds = completions.map { it.habitId }.toSet()
            habits
                .filter { it.matches(date.dayOfWeek) }
                .map { entity ->
                    Habit(
                        id = entity.id,
                        title = entity.title,
                        iconKey = entity.iconKey,
                        colorKey = entity.colorKey,
                        frequencyType = HabitFrequencyType.valueOf(entity.frequencyType),
                        customDays = parseDays(entity.customDaysCsv),
                        reminderEnabled = entity.reminderEnabled,
                        isCompletedForSelectedDate = completionIds.contains(entity.id),
                        streakDays = habitDao.getCompletionCountByHabit(entity.id)
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
    }

    override suspend fun createHabit(draft: HabitCreateDraft) {
        val customDaysCsv = draft.customDays
            .sortedBy { it.value }
            .joinToString(",") { it.value.toString() }

        habitDao.insertHabit(
            HabitEntity(
                cloudId = UUID.randomUUID().toString(),
                title = draft.title,
                iconKey = draft.iconKey,
                colorKey = draft.colorKey,
                frequencyType = draft.frequencyType.name,
                customDaysCsv = customDaysCsv,
                reminderEnabled = draft.reminderEnabled,
                reminderHour = 20,
                reminderMinute = 0,
                createdAt = System.currentTimeMillis(),
                isArchived = false,
                source = "manual"
            )
        )
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
                    isArchived = false,
                    source = "onboarding"
                )
            }
        }

        if (entities.isNotEmpty()) {
            habitDao.insertHabits(entities)
        }
    }

    override suspend fun getIncompleteHabitsForDate(date: LocalDate): List<Habit> {
        val habits = habitDao.getActiveHabits().filter { it.matches(date.dayOfWeek) }
        val completionIds = completionDao.getCompletionsForDate(date.toEpochDay()).map { it.habitId }.toSet()

        return habits
            .filterNot { completionIds.contains(it.id) }
            .map { entity ->
                Habit(
                    id = entity.id,
                    title = entity.title,
                    iconKey = entity.iconKey,
                    colorKey = entity.colorKey,
                    frequencyType = HabitFrequencyType.valueOf(entity.frequencyType),
                    customDays = parseDays(entity.customDaysCsv),
                    reminderEnabled = entity.reminderEnabled,
                    isCompletedForSelectedDate = false,
                    streakDays = habitDao.getCompletionCountByHabit(entity.id)
                )
            }
    }

    private fun HabitEntity.matches(dayOfWeek: DayOfWeek): Boolean {
        return when (HabitFrequencyType.valueOf(frequencyType)) {
            HabitFrequencyType.DAILY -> true
            HabitFrequencyType.WEEKDAYS -> dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            HabitFrequencyType.CUSTOM -> parseDays(customDaysCsv).contains(dayOfWeek)
        }
    }

    private fun parseDays(csv: String): Set<DayOfWeek> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",")
            .mapNotNull { value -> value.toIntOrNull() }
            .mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }
            .toSet()
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
}

data class OnboardingHabitDefault(
    val key: String,
    val title: String,
    val iconKey: String,
    val colorKey: String
)
