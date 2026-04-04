package com.vadymdev.habitix.data.repository.habit

import com.vadymdev.habitix.data.local.room.AchievementUnlockEntity
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.model.ProfileAchievement
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HabitInsightsCalculator {

    fun buildProfileAnalytics(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        persistedUnlocks: List<AchievementUnlockEntity>,
        today: LocalDate = LocalDate.now()
    ): ProfileAnalytics {
        val detailsBuilder = HabitInsightsDetailsBuilder(this)
        val completionDates = completions.map { it.dateEpochDay }.distinct().sorted()
        val completionByDate = completions.groupBy { it.dateEpochDay }
        val bestStreak = longestStreakForDates(completionDates)
        val currentStreak = currentStreakForDates(completionDates, today.toEpochDay())
        val totalCompleted = completions.size

        val earliestCreatedDate = habits.minOfOrNull {
            Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        val daysWithUs = earliestCreatedDate?.let {
            (today.toEpochDay() - it.toEpochDay()).toInt() + 1
        }?.coerceAtLeast(0) ?: 0

        val thisMonthStart = today.withDayOfMonth(1)
        val lastMonthStart = thisMonthStart.minusMonths(1)
        val thisMonthEnd = thisMonthStart.plusMonths(1).minusDays(1)
        val lastMonthEnd = thisMonthStart.minusDays(1)
        val thisMonthCount = countCompletionsInRange(completionByDate, thisMonthStart, thisMonthEnd)
        val lastMonthCount = countCompletionsInRange(completionByDate, lastMonthStart, lastMonthEnd)

        val monthGrowthPercent = if (lastMonthCount == 0) {
            if (thisMonthCount == 0) 0 else 100
        } else {
            (((thisMonthCount - lastMonthCount) * 100f) / lastMonthCount).toInt()
        }

        val monthWeeklyActivity = buildList {
            repeat(4) { block ->
                val start = thisMonthStart.plusDays((block * 7L))
                val end = minOf(thisMonthEnd, start.plusDays(6))
                add(countCompletionsInRange(completionByDate, start, end))
            }
        }

        val achievements = detailsBuilder.buildAchievementsWithDates(habits = habits, completions = completions, today = today)
        val persistedDates = persistedUnlocks.associate { it.achievementId to LocalDate.ofEpochDay(it.unlockedEpochDay) }
        val achievementsWithPersistedDates = achievements.map { achievement ->
            val persistedDate = persistedDates[achievement.id]
            if (persistedDate != null && achievement.unlocked) {
                achievement.copy(unlockedDate = persistedDate)
            } else {
                achievement
            }
        }

        val unlockedXp = achievementsWithPersistedDates.filter { it.unlocked }.sumOf { it.xpReward }
        val totalXp = totalCompleted * 5 + unlockedXp
        val level = (totalXp / 100).coerceAtLeast(1)
        val xpCurrent = totalXp % 1000

        return ProfileAnalytics(
            level = level,
            xpCurrent = xpCurrent,
            xpTarget = 1000,
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalCompleted = totalCompleted,
            daysWithUs = daysWithUs,
            monthGrowthPercent = monthGrowthPercent,
            monthWeeklyActivity = monthWeeklyActivity,
            topAchievements = achievementsWithPersistedDates.take(3),
            allAchievements = achievementsWithPersistedDates
        )
    }

    fun buildStatsSnapshot(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        periodDays: Int,
        today: LocalDate = LocalDate.now()
    ): HabitStatsSnapshot {
        val detailsBuilder = HabitInsightsDetailsBuilder(this)
        val activeHabits = habits.filter { !it.isArchived }
        val completionsByHabit = completions.groupBy { it.habitId }
        val completionByDate = completions.groupBy { it.dateEpochDay }

        val completedTasks = completions.size
        val longestStreak = activeHabits.maxOfOrNull { habit ->
            longestStreakForDates(completionsByHabit[habit.id].orEmpty().map { it.dateEpochDay })
        } ?: 0

        var totalPossible = 0
        var totalDone = 0

        repeat(periodDays) { offset ->
            val date = today.minusDays((periodDays - 1 - offset).toLong())
            val epoch = date.toEpochDay()
            val possibleToday = activeHabits.count { matches(it, date) }
            totalPossible += possibleToday
            totalDone += completionByDate[epoch]?.size ?: 0
        }

        val successRate = if (totalPossible == 0) 0 else ((totalDone * 100f) / totalPossible).toInt().coerceIn(0, 100)

        val heatmapCounts = buildList {
            repeat(periodDays) { idx ->
                val date = today.minusDays((periodDays - 1 - idx).toLong())
                add(completionByDate[date.toEpochDay()]?.size ?: 0)
            }
        }

        val heatmapLevels = buildList {
            val maxCount = heatmapCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
            repeat(periodDays) { idx ->
                val count = heatmapCounts[idx]
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

        val categoryStats = detailsBuilder.buildCategoryStats(activeHabits, completionsByHabit)
        val badges = detailsBuilder.buildBadges(activeHabits, completions, longestStreak)

        return HabitStatsSnapshot(
            longestStreak = longestStreak,
            earnedBadgesCount = badges.count { it.earned },
            successRatePercent = successRate,
            completedTasksCount = completedTasks,
            heatmapLevels = heatmapLevels,
            heatmapCounts = heatmapCounts,
            heatmapStartEpochDay = today.minusDays((periodDays - 1).toLong()).toEpochDay(),
            categoryStats = categoryStats,
            badges = badges
        )
    }

    fun computeUnlockedAchievements(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        today: LocalDate = LocalDate.now()
    ): List<AchievementUnlockEntity> {
        val detailsBuilder = HabitInsightsDetailsBuilder(this)
        return detailsBuilder.buildAchievementsWithDates(habits = habits, completions = completions, today = today)
            .asSequence()
            .filter { it.unlocked }
            .map { achievement ->
                AchievementUnlockEntity(
                    achievementId = achievement.id,
                    unlockedEpochDay = (achievement.unlockedDate ?: today).toEpochDay()
                )
            }
            .toList()
    }

    fun parseDays(csv: String): Set<DayOfWeek> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",")
            .mapNotNull { value -> value.toIntOrNull() }
            .mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }
            .toSet()
    }

    fun matches(habit: HabitEntity, date: LocalDate): Boolean {
        val epochDay = date.toEpochDay()
        val dayOfWeek = date.dayOfWeek
        if (epochDay < habit.startEpochDay) return false
        if (habit.activeUntilEpochDay != null && epochDay > habit.activeUntilEpochDay) return false

        return when (HabitFrequencyType.valueOf(habit.frequencyType)) {
            HabitFrequencyType.DAILY -> true
            HabitFrequencyType.WEEKDAYS -> dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            HabitFrequencyType.CUSTOM -> parseDays(habit.customDaysCsv).contains(dayOfWeek)
        }
    }

    internal fun longestStreakForDates(epochDays: List<Long>): Int {
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

    internal fun currentStreakForDates(sortedEpochDates: List<Long>, todayEpoch: Long): Int {
        if (sortedEpochDates.isEmpty()) return 0
        var cursor = todayEpoch
        var streak = 0
        val dateSet = sortedEpochDates.toSet()

        while (dateSet.contains(cursor)) {
            streak += 1
            cursor -= 1
        }

        return streak
    }

    internal fun hourOf(timestampMillis: Long): Int {
        return Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .hour
    }

    internal fun countCompletionsInRange(
        completionByDate: Map<Long, List<HabitCompletionEntity>>,
        start: LocalDate,
        end: LocalDate
    ): Int {
        if (end.isBefore(start)) return 0
        var sum = 0
        var cursor = start
        while (!cursor.isAfter(end)) {
            sum += completionByDate[cursor.toEpochDay()]?.size ?: 0
            cursor = cursor.plusDays(1)
        }
        return sum
    }
}
