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
        val completionDates = completions.map { it.dateEpochDay }.distinct().sorted()
        val completionByDate = completions.groupBy { it.dateEpochDay }
        val completionByHabit = completions.groupBy { it.habitId }
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

        val achievements = buildAchievementsWithDates(habits = habits, completions = completions, today = today)
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
            repeat(15 * 7) { idx ->
                val date = today.minusDays((15 * 7 - 1 - idx).toLong())
                add(completionByDate[date.toEpochDay()]?.size ?: 0)
            }
        }

        val heatmapLevels = buildList {
            val maxCount = heatmapCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
            repeat(15 * 7) { idx ->
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

        val categoryStats = buildCategoryStats(activeHabits, completionsByHabit)
        val badges = buildBadges(activeHabits, completions, longestStreak)

        return HabitStatsSnapshot(
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

    fun computeUnlockedAchievements(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        today: LocalDate = LocalDate.now()
    ): List<AchievementUnlockEntity> {
        return buildAchievementsWithDates(habits = habits, completions = completions, today = today)
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

    private fun buildAchievementsWithDates(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        today: LocalDate
    ): List<ProfileAchievement> {
        val completionByHabit = completions.groupBy { it.habitId }
        val completionDates = completions.map { it.dateEpochDay }.distinct().sorted()
        val activeHabits = habits.filter { !it.isArchived }
        val completionByDate = completions.groupBy { it.dateEpochDay }

        val bestStreak = longestStreakForDates(completionDates)
        val earlyBefore8 = completions.count { hourOf(it.completedAtMillis) < 8 }
        val earlyBefore7 = completions.count { hourOf(it.completedAtMillis) < 7 }
        val lateAfter22 = completions.count { hourOf(it.completedAtMillis) >= 22 }
        val createdHabitsCount = habits.size
        val monthlyPerfectProgress = monthlyPerfectionPercent(activeHabits, completionByDate, today)
        val perfectWeekScore = perfectWeekCount(activeHabits, completionByDate, today)
        val healthCount = completionsByColor(habits, completionByHabit, setOf("mint", "green", "rose"))
        val sportCount = completionsByColor(habits, completionByHabit, setOf("blue", "sky"))
        val mindfulCount = completionsByColor(habits, completionByHabit, setOf("purple", "lavender"))
        val productiveCount = completionsByColor(habits, completionByHabit, setOf("orange", "peach"))
        val readingStreak = habits
            .filter { it.title.contains("чит", ignoreCase = true) || it.iconKey == "book" }
            .maxOfOrNull { h -> longestStreakForDates(completionByHabit[h.id].orEmpty().map { it.dateEpochDay }) }
            ?: 0

        val unlockDatesById = buildAchievementUnlockDates(
            habits = habits,
            completions = completions,
            completionByHabit = completionByHabit,
            completionDates = completionDates,
            monthlyPerfectPercent = monthlyPerfectProgress,
            perfectWeekCount = perfectWeekScore,
            today = today
        )

        return buildProfileAchievements(
            bestStreak = bestStreak,
            earlyBefore8 = earlyBefore8,
            earlyBefore7 = earlyBefore7,
            lateAfter22 = lateAfter22,
            monthlyPerfectPercent = monthlyPerfectProgress,
            perfectWeekCount = perfectWeekScore,
            createdHabitsCount = createdHabitsCount,
            healthCount = healthCount,
            sportCount = sportCount,
            mindfulCount = mindfulCount,
            readingStreak = readingStreak,
            productiveCount = productiveCount,
            unlockedDateById = unlockDatesById
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

    private fun currentStreakForDates(sortedEpochDates: List<Long>, todayEpoch: Long): Int {
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

    private fun hourOf(timestampMillis: Long): Int {
        return Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .hour
    }

    private fun countCompletionsInRange(
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

    private fun perfectWeekCount(
        habits: List<HabitEntity>,
        completionByDate: Map<Long, List<HabitCompletionEntity>>,
        today: LocalDate
    ): Int {
        var perfectWeeks = 0
        repeat(8) { back ->
            val end = today.minusDays((back * 7L))
            val start = end.minusDays(6)
            var allPerfect = true
            var cursor = start
            while (!cursor.isAfter(end)) {
                val possible = habits.count { matches(it, cursor) }
                val done = completionByDate[cursor.toEpochDay()]?.size ?: 0
                if (possible > 0 && done < possible) {
                    allPerfect = false
                    break
                }
                cursor = cursor.plusDays(1)
            }
            if (allPerfect) perfectWeeks += 1
        }
        return perfectWeeks
    }

    private fun monthlyPerfectionPercent(
        habits: List<HabitEntity>,
        completionByDate: Map<Long, List<HabitCompletionEntity>>,
        today: LocalDate
    ): Int {
        val start = today.withDayOfMonth(1)
        val end = today.withDayOfMonth(today.lengthOfMonth())
        var possible = 0
        var done = 0
        var cursor = start
        while (!cursor.isAfter(end)) {
            val count = habits.count { matches(it, cursor) }
            possible += count
            done += completionByDate[cursor.toEpochDay()]?.size ?: 0
            cursor = cursor.plusDays(1)
        }
        if (possible == 0) return 0
        return ((done * 100f) / possible).toInt().coerceIn(0, 100)
    }

    private fun completionsByColor(
        habits: List<HabitEntity>,
        completionByHabit: Map<Long, List<HabitCompletionEntity>>,
        colorKeys: Set<String>
    ): Int {
        return habits
            .filter { colorKeys.contains(it.colorKey) }
            .sumOf { completionByHabit[it.id].orEmpty().size }
    }

    private fun buildProfileAchievements(
        bestStreak: Int,
        earlyBefore8: Int,
        earlyBefore7: Int,
        lateAfter22: Int,
        monthlyPerfectPercent: Int,
        perfectWeekCount: Int,
        createdHabitsCount: Int,
        healthCount: Int,
        sportCount: Int,
        mindfulCount: Int,
        readingStreak: Int,
        productiveCount: Int,
        unlockedDateById: Map<String, LocalDate?>
    ): List<ProfileAchievement> {
        fun mk(
            id: String,
            title: String,
            description: String,
            icon: String,
            color: String,
            category: String,
            xp: Int,
            value: Int,
            target: Int
        ): ProfileAchievement {
            val progress = if (target <= 0) 0 else ((value * 100f) / target).toInt().coerceIn(0, 100)
            val unlocked = progress >= 100
            return ProfileAchievement(
                id = id,
                title = title,
                description = description,
                iconKey = icon,
                colorKey = color,
                category = category,
                xpReward = xp,
                progressPercent = progress,
                unlocked = unlocked,
                unlockedDate = if (unlocked) unlockedDateById[id] else null
            )
        }

        return listOf(
            mk("week_7", "7-денна серія", "Виконуйте звичку 7 днів поспіль", "flame", "peach", "Серії", 50, bestStreak, 7),
            mk("week_14", "14-денна серія", "Виконуйте звичку 14 днів поспіль", "flame", "peach", "Серії", 100, bestStreak, 14),
            mk("week_30", "Марафонець", "30-денна серія", "medal", "rose", "Серії", 200, bestStreak, 30),
            mk("week_100", "Легенда", "100-денна серія", "crown", "lavender", "Серії", 500, bestStreak, 100),
            mk("early_8", "Ранній птах", "Виконайте 5 звичок до 8:00", "zap", "sky", "Час", 75, earlyBefore8, 5),
            mk("early_7", "Світанковий воїн", "Виконайте 20 звичок до 7:00", "sunrise", "peach", "Час", 150, earlyBefore7, 20),
            mk("late_owl", "Нічна сова", "Виконайте 10 звичок після 22:00", "moon", "lavender", "Час", 75, lateAfter22, 10),
            mk("month_perfect", "Місячний чемпіон", "100% виконання за місяць", "crown", "lavender", "Досконалість", 300, monthlyPerfectPercent, 100),
            mk("perfect_week", "Перфекціоніст", "Ідеальний тиждень", "target", "mint", "Досконалість", 100, perfectWeekCount, 1),
            mk("first", "Перша перемога", "Створіть першу звичку", "star", "mint", "Початок", 10, createdHabitsCount, 1),
            mk("five", "Колекціонер", "Створіть 5 різних звичок", "sparkles", "sky", "Початок", 50, createdHabitsCount, 5),
            mk("ten", "Амбіційний", "Створіть 10 звичок", "trophy", "peach", "Початок", 100, createdHabitsCount, 10),
            mk("health", "Здоровий спосіб життя", "Виконайте 50 звичок категорії Здоров'я", "heart", "rose", "Категорії", 150, healthCount, 50),
            mk("sport", "Спортсмен", "Виконайте 30 звичок категорії Спорт", "dumbbell", "mint", "Категорії", 150, sportCount, 30),
            mk("mind", "Мудрець", "Виконайте 50 звичок категорії Усвідомленість", "brain", "lavender", "Категорії", 150, mindfulCount, 50),
            mk("book", "Книголюб", "Читайте 30 днів поспіль", "book", "sky", "Категорії", 200, readingStreak, 30),
            mk("prod", "Продуктивний", "Виконайте 100 звичок категорії Продуктивність", "coffee", "peach", "Категорії", 250, productiveCount, 100)
        )
    }

    private fun buildCategoryStats(
        activeHabits: List<HabitEntity>,
        completionsByHabit: Map<Long, List<HabitCompletionEntity>>
    ): List<HabitCategoryStat> {
        data class CategoryBucket(val name: String, val colorKey: String, val keys: Set<String>)

        val buckets = listOf(
            CategoryBucket("Здоров'я", "mint", setOf("mint", "green", "rose")),
            CategoryBucket("Продуктивність", "orange", setOf("orange", "peach")),
            CategoryBucket("Спорт", "blue", setOf("blue", "sky")),
            CategoryBucket("Усвідомленість", "purple", setOf("purple", "lavender"))
        )

        val counts = buckets.associateWith { bucket ->
            activeHabits
                .filter { habit -> bucket.keys.contains(habit.colorKey) }
                .sumOf { habit -> completionsByHabit[habit.id].orEmpty().size }
        }

        val total = counts.values.sum().coerceAtLeast(1)
        return buckets.map { bucket ->
            HabitCategoryStat(
                name = bucket.name,
                colorKey = bucket.colorKey,
                percent = ((counts.getValue(bucket) * 100f) / total).toInt().coerceIn(0, 100)
            )
        }
    }

    private fun buildAchievementUnlockDates(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        completionByHabit: Map<Long, List<HabitCompletionEntity>>,
        completionDates: List<Long>,
        monthlyPerfectPercent: Int,
        perfectWeekCount: Int,
        today: LocalDate
    ): Map<String, LocalDate?> {
        val sortedCompletions = completions.sortedBy { it.completedAtMillis }
        val result = mutableMapOf<String, LocalDate?>()

        result["week_7"] = dateWhenStreakReached(completionDates, 7)
        result["week_14"] = dateWhenStreakReached(completionDates, 14)
        result["week_30"] = dateWhenStreakReached(completionDates, 30)
        result["week_100"] = dateWhenStreakReached(completionDates, 100)

        result["early_8"] = dateWhenCountReached(sortedCompletions.filter { hourOf(it.completedAtMillis) < 8 }, 5)
        result["early_7"] = dateWhenCountReached(sortedCompletions.filter { hourOf(it.completedAtMillis) < 7 }, 20)
        result["late_owl"] = dateWhenCountReached(sortedCompletions.filter { hourOf(it.completedAtMillis) >= 22 }, 10)

        if (monthlyPerfectPercent >= 100) {
            result["month_perfect"] = today
        }
        if (perfectWeekCount >= 1) {
            result["perfect_week"] = today
        }

        val habitsByCreated = habits.sortedBy { it.createdAt }
        result["first"] = habitsByCreated.getOrNull(0)?.createdAt?.toLocalDateFromMillis()
        result["five"] = habitsByCreated.getOrNull(4)?.createdAt?.toLocalDateFromMillis()
        result["ten"] = habitsByCreated.getOrNull(9)?.createdAt?.toLocalDateFromMillis()

        result["health"] = dateWhenCountReached(
            completionsForColors(habits, completionByHabit, setOf("mint", "green", "rose")),
            50
        )
        result["sport"] = dateWhenCountReached(
            completionsForColors(habits, completionByHabit, setOf("blue", "sky")),
            30
        )
        result["mind"] = dateWhenCountReached(
            completionsForColors(habits, completionByHabit, setOf("purple", "lavender")),
            50
        )
        result["book"] = dateWhenStreakReached(
            habits
                .filter { it.title.contains("чит", ignoreCase = true) || it.iconKey == "book" }
                .flatMap { completionByHabit[it.id].orEmpty() }
                .map { it.dateEpochDay }
                .distinct()
                .sorted(),
            30
        )
        result["prod"] = dateWhenCountReached(
            completionsForColors(habits, completionByHabit, setOf("orange", "peach")),
            100
        )

        return result
    }

    private fun completionsForColors(
        habits: List<HabitEntity>,
        completionByHabit: Map<Long, List<HabitCompletionEntity>>,
        colorKeys: Set<String>
    ): List<HabitCompletionEntity> {
        return habits
            .filter { colorKeys.contains(it.colorKey) }
            .flatMap { completionByHabit[it.id].orEmpty() }
            .sortedBy { it.completedAtMillis }
    }

    private fun dateWhenCountReached(
        completions: List<HabitCompletionEntity>,
        target: Int
    ): LocalDate? {
        if (target <= 0 || completions.size < target) return null
        return completions[target - 1].dateEpochDay.toLocalDateFromEpochDay()
    }

    private fun dateWhenStreakReached(epochDaysSorted: List<Long>, target: Int): LocalDate? {
        if (target <= 0 || epochDaysSorted.isEmpty()) return null
        var current = 1
        if (target == 1) return epochDaysSorted.first().toLocalDateFromEpochDay()

        for (index in 1 until epochDaysSorted.size) {
            current = if (epochDaysSorted[index] == epochDaysSorted[index - 1] + 1) {
                current + 1
            } else {
                1
            }

            if (current >= target) {
                return epochDaysSorted[index].toLocalDateFromEpochDay()
            }
        }
        return null
    }

    private fun Long.toLocalDateFromEpochDay(): LocalDate {
        return LocalDate.ofEpochDay(this)
    }

    private fun Long.toLocalDateFromMillis(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
