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
import com.vadymdev.habitix.domain.model.ProfileAchievement
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import com.vadymdev.habitix.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.ZoneId
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val hiddenDayDao: HiddenHabitDayDao
) : HabitRepository {

    override fun observeProfileAnalytics(): Flow<ProfileAnalytics> {
        return combine(
            habitDao.observeAllHabits(),
            completionDao.observeAllCompletions()
        ) { habits, completions ->
            val today = LocalDate.now()
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

            val activeHabits = habits.filter { !it.isArchived }
            val createdHabitsCount = habits.size
            val earlyBefore8 = completions.count { hourOf(it.completedAtMillis) < 8 }
            val earlyBefore7 = completions.count { hourOf(it.completedAtMillis) < 7 }
            val lateAfter22 = completions.count { hourOf(it.completedAtMillis) >= 22 }
            val perfectWeekScore = perfectWeekCount(activeHabits, completionByDate)
            val monthlyPerfectProgress = monthlyPerfectionPercent(activeHabits, completionByDate, today)
            val healthCount = completionsByColor(habits, completionByHabit, setOf("mint", "green", "rose"))
            val sportCount = completionsByColor(habits, completionByHabit, setOf("blue", "sky"))
            val mindfulCount = completionsByColor(habits, completionByHabit, setOf("purple", "lavender"))
            val productiveCount = completionsByColor(habits, completionByHabit, setOf("orange", "peach"))
            val readingStreak = habits
                .filter { it.title.contains("чит", ignoreCase = true) || it.iconKey == "book" }
                .maxOfOrNull { h -> longestStreakForDates(completionByHabit[h.id].orEmpty().map { it.dateEpochDay }) }
                ?: 0

            val achievements = buildProfileAchievements(
                today = today,
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
                productiveCount = productiveCount
            )

            val unlockedXp = achievements.filter { it.unlocked }.sumOf { it.xpReward }
            val totalXp = totalCompleted * 5 + unlockedXp
            val level = (totalXp / 100).coerceAtLeast(1)
            val xpCurrent = totalXp % 1000

            ProfileAnalytics(
                level = level,
                xpCurrent = xpCurrent,
                xpTarget = 1000,
                currentStreakDays = currentStreak,
                bestStreakDays = bestStreak,
                totalCompleted = totalCompleted,
                daysWithUs = daysWithUs,
                monthGrowthPercent = monthGrowthPercent,
                monthWeeklyActivity = monthWeeklyActivity,
                topAchievements = achievements.take(3),
                allAchievements = achievements
            )
        }
    }

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
        completionByDate: Map<Long, List<HabitCompletionEntity>>
    ): Int {
        val today = LocalDate.now()
        var perfectWeeks = 0
        repeat(8) { back ->
            val end = today.minusDays((back * 7L))
            val start = end.minusDays(6)
            var allPerfect = true
            var cursor = start
            while (!cursor.isAfter(end)) {
                val possible = habits.count { it.matches(cursor) }
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
            val count = habits.count { it.matches(cursor) }
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
        today: LocalDate,
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
        productiveCount: Int
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
                unlockedDate = if (unlocked) today.minusDays((id.hashCode().toLong() and 7L) + 1L) else null
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
}

data class OnboardingHabitDefault(
    val key: String,
    val title: String,
    val iconKey: String,
    val colorKey: String
)
