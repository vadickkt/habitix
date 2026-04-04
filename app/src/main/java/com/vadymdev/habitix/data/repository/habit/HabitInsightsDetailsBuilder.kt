package com.vadymdev.habitix.data.repository.habit

import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.ProfileAchievement
import java.time.LocalDate

internal class HabitInsightsDetailsBuilder(
    private val calculator: HabitInsightsCalculator
) {

    fun buildAchievementsWithDates(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        today: LocalDate
    ): List<ProfileAchievement> {
        val completionByHabit = completions.groupBy { it.habitId }
        val completionDates = completions.map { it.dateEpochDay }.distinct().sorted()
        val activeHabits = habits.filter { !it.isArchived }
        val completionByDate = completions.groupBy { it.dateEpochDay }

        val bestStreak = calculator.longestStreakForDates(completionDates)
        val earlyBefore8 = completions.count { calculator.hourOf(it.completedAtMillis) < 8 }
        val earlyBefore7 = completions.count { calculator.hourOf(it.completedAtMillis) < 7 }
        val lateAfter22 = completions.count { calculator.hourOf(it.completedAtMillis) >= 22 }
        val createdHabitsCount = habits.size
        val monthlyPerfectProgress = monthlyPerfectionPercent(activeHabits, completionByDate, today)
        val perfectWeekScore = perfectWeekCount(activeHabits, completionByDate, today)
        val healthCount = completionsByColor(habits, completionByHabit, setOf("mint", "green", "rose"))
        val sportCount = completionsByColor(habits, completionByHabit, setOf("blue", "sky"))
        val mindfulCount = completionsByColor(habits, completionByHabit, setOf("purple", "lavender"))
        val productiveCount = completionsByColor(habits, completionByHabit, setOf("orange", "peach"))
        val readingStreak = habits
            .filter { it.title.contains("чит", ignoreCase = true) || it.iconKey == "book" }
            .maxOfOrNull { h -> calculator.longestStreakForDates(completionByHabit[h.id].orEmpty().map { it.dateEpochDay }) }
            ?: 0

        val unlockDatesById = buildAchievementUnlockDates(
            habits = habits,
            completions = completions,
            completionByHabit = completionByHabit,
            completionDates = completionDates,
            monthlyPerfectPercent = monthlyPerfectProgress,
            perfectWeekCount = perfectWeekScore,
            today = today,
            calculator = calculator
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

    fun buildBadges(
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

    fun buildCategoryStats(
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
                val possible = habits.count { calculator.matches(it, cursor) }
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
            val count = habits.count { calculator.matches(it, cursor) }
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

}
