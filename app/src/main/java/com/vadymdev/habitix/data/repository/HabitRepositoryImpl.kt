package com.vadymdev.habitix.data.repository

import com.vadymdev.habitix.data.local.room.HabitCompletionDao
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitDao
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.data.local.room.HiddenHabitDayDao
import com.vadymdev.habitix.data.local.room.HiddenHabitDayEntity
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val hiddenDayDao: HiddenHabitDayDao
) : HabitRepository {

    override fun observeStats(periodDays: Int): Flow<HabitStatsSnapshot> {
        return combine(
            habitDao.observeAllHabits(),
            completionDao.observeAllCompletions()
        ) { habits, completions ->
            val activeHabits = habits.filter { !it.isArchived }
            val completionsByHabit = completions.groupBy { it.habitId }
            val completionByDate = completions.groupBy { it.dateEpochDay }

            val completedTasks = completions.size
            val longestStreak = activeHabits.maxOfOrNull { habit ->
                longestStreakForDates(completionsByHabit[habit.id].orEmpty().map { it.dateEpochDay })
            } ?: 0

            val today = LocalDate.now()
            var totalPossible = 0
            var totalDone = 0

            repeat(periodDays) { offset ->
                val date = today.minusDays((periodDays - 1 - offset).toLong())
                val epoch = date.toEpochDay()
                val possibleToday = activeHabits.count { it.matches(date) }
                totalPossible += possibleToday
                totalDone += completionByDate[epoch]?.size ?: 0
            }

            val successRate = if (totalPossible == 0) 0 else ((totalDone * 100f) / totalPossible).toInt().coerceIn(0, 100)

            val heatmapCounts = buildList {
                repeat(15 * 7) { idx ->
                    val date = today.minusDays((15 * 7 - 1 - idx).toLong())
                    add(completionByDate[date.toEpochDay()]?.size ?: 0)
                }
            }

            val heatmapLevels = buildList {
                val allCounts = heatmapCounts
                val maxCount = allCounts.maxOrNull()?.coerceAtLeast(1) ?: 1

                repeat(15 * 7) { idx ->
                    val count = allCounts[idx]
                    val ratio = count.toFloat() / maxCount.toFloat()
                    add(
                        when {
                            count <= 0 -> 0
                            ratio <= 0.25f -> 1
                            ratio <= 0.5f -> 2
                            ratio <= 0.75f -> 3
                            else -> 4
                        }
                    )
                }
            }

            val categories = listOf(
                "Здоров'я" to "mint",
                "Продуктивність" to "orange",
                "Спорт" to "blue",
                "Усвідомленість" to "purple"
            )

            val categoryGrowth = IntArray(categories.size)
            completionByDate.keys.sorted().forEach { epochDay ->
                // Slow growth model: each active day gives +1% to one pseudo-random category.
                val index = abs((epochDay * 31 + 17).toInt()) % categories.size
                categoryGrowth[index] = (categoryGrowth[index] + 1).coerceAtMost(100)
            }

            val categoryStats = categories.mapIndexed { index, (title, colorKey) ->
                HabitCategoryStat(
                    name = title,
                    percent = categoryGrowth[index],
                    colorKey = colorKey
                )
            }

            val badges = buildBadges(activeHabits, completions, longestStreak)

            HabitStatsSnapshot(
                longestStreak = longestStreak,
                earnedBadgesCount = badges.count { it.earned },
                successRatePercent = successRate,
                completedTasksCount = completedTasks,
                heatmapLevels = heatmapLevels,
                heatmapCounts = heatmapCounts,
                heatmapStartEpochDay = today.minusDays((15 * 7 - 1).toLong()).toEpochDay(),
                categoryStats = categoryStats,
                badges = badges
            )
        }
    }

    override fun observeHabitsForDate(date: LocalDate): Flow<List<Habit>> {
        val epochDay = date.toEpochDay()
        return combine(
            habitDao.observeActiveHabits(),
            completionDao.observeCompletionsForDate(epochDay),
            hiddenDayDao.observeHiddenForDate(epochDay)
        ) { habits, completions, hiddenDays ->
            val completionIds = completions.map { it.habitId }.toSet()
            val hiddenIds = hiddenDays.map { it.habitId }.toSet()
            habits
                .filter { it.matches(date) }
                .filterNot { hiddenIds.contains(it.id) }
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
    }

    override suspend fun hideHabitForDate(habitId: Long, date: LocalDate) {
        val epochDay = date.toEpochDay()
        completionDao.removeCompletion(habitId, epochDay)
        hiddenDayDao.upsert(HiddenHabitDayEntity(habitId = habitId, dateEpochDay = epochDay))
    }

    override suspend fun deactivateHabitFromDate(habitId: Long, date: LocalDate) {
        val epochDay = date.toEpochDay()
        completionDao.removeCompletion(habitId, epochDay)
        habitDao.updateActiveUntil(habitId, epochDay - 1)
    }

    override suspend fun deleteAllHabits() {
        completionDao.removeAll()
        hiddenDayDao.deleteAll()
        habitDao.deleteAllHabits()
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
                activeUntilEpochDay = null,
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
                    activeUntilEpochDay = null,
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
        val habits = habitDao.getActiveHabits().filter { it.matches(date) }
        val completionIds = completionDao.getCompletionsForDate(date.toEpochDay()).map { it.habitId }.toSet()
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
                    customDays = parseDays(entity.customDaysCsv),
                    reminderEnabled = entity.reminderEnabled,
                    isCompletedForSelectedDate = false,
                    streakDays = habitDao.getCompletionCountByHabit(entity.id)
                )
            }
    }

    private fun HabitEntity.matches(date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek
        if (activeUntilEpochDay != null && date.toEpochDay() > activeUntilEpochDay) return false

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

    private fun longestStreakForDates(epochDays: List<Long>): Int {
        if (epochDays.isEmpty()) return 0
        val sorted = epochDays.distinct().sorted()
        var best = 1
        var current = 1

        for (index in 1 until sorted.size) {
            if (sorted[index] == sorted[index - 1] + 1) {
                current += 1
                if (current > best) best = current
            } else {
                current = 1
            }
        }

        return best
    }

    private fun buildBadges(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        longestStreak: Int
    ): List<HabitBadge> {
        val completedCount = completions.size
        val completionsByHabit = completions.groupBy { it.habitId }
        val meditationCount = habits.filter { it.iconKey == "mind" }.sumOf { completionsByHabit[it.id].orEmpty().size }
        val bookCount = habits.filter { it.iconKey == "book" }.sumOf { completionsByHabit[it.id].orEmpty().size }

        return listOf(
            HabitBadge("week_1", "Перший тиждень", "🌱", completedCount >= 7),
            HabitBadge("days_30", "30 днів", "🔥", longestStreak >= 30 || completedCount >= 30),
            HabitBadge("days_100", "100 днів", "💎", longestStreak >= 100 || completedCount >= 100),
            HabitBadge("morning", "Ранкова пташка", "🐦", completedCount >= 20),
            HabitBadge("mind", "Майстер медитації", "🧘", meditationCount >= 20),
            HabitBadge("book", "Книжковий черв'як", "📚", bookCount >= 20)
        )
    }
}

data class OnboardingHabitDefault(
    val key: String,
    val title: String,
    val iconKey: String,
    val colorKey: String
)
