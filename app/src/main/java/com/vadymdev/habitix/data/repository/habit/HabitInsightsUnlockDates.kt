package com.vadymdev.habitix.data.repository.habit

import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun buildAchievementUnlockDates(
    habits: List<HabitEntity>,
    completions: List<HabitCompletionEntity>,
    completionByHabit: Map<Long, List<HabitCompletionEntity>>,
    completionDates: List<Long>,
    monthlyPerfectPercent: Int,
    perfectWeekCount: Int,
    today: LocalDate,
    calculator: HabitInsightsCalculator
): Map<String, LocalDate?> {
    val sortedCompletions = completions.sortedBy { it.completedAtMillis }
    val result = mutableMapOf<String, LocalDate?>()

    result["week_7"] = dateWhenStreakReached(completionDates, 7)
    result["week_14"] = dateWhenStreakReached(completionDates, 14)
    result["week_30"] = dateWhenStreakReached(completionDates, 30)
    result["week_100"] = dateWhenStreakReached(completionDates, 100)

    result["early_8"] = dateWhenCountReached(sortedCompletions.filter { calculator.hourOf(it.completedAtMillis) < 8 }, 5)
    result["early_7"] = dateWhenCountReached(sortedCompletions.filter { calculator.hourOf(it.completedAtMillis) < 7 }, 20)
    result["late_owl"] = dateWhenCountReached(sortedCompletions.filter { calculator.hourOf(it.completedAtMillis) >= 22 }, 10)

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
