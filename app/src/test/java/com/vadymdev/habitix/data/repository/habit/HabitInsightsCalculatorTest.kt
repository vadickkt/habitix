package com.vadymdev.habitix.data.repository.habit

import com.vadymdev.habitix.data.local.room.AchievementUnlockEntity
import com.vadymdev.habitix.data.local.room.HabitCompletionEntity
import com.vadymdev.habitix.data.local.room.HabitEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset

class HabitInsightsCalculatorTest {

    private val calculator = HabitInsightsCalculator()

    @Test
    fun parseDays_parsesCsvToSet() {
        val parsed = calculator.parseDays("1,3,5")

        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), parsed)
    }

    @Test
    fun matches_weekdays_excludesWeekend() {
        val habit = HabitEntity(
            id = 1,
            cloudId = "c1",
            title = "Test",
            iconKey = "water",
            colorKey = "mint",
            frequencyType = "WEEKDAYS",
            customDaysCsv = "",
            reminderEnabled = true,
            reminderHour = 9,
            reminderMinute = 0,
            createdAt = 0,
            startEpochDay = LocalDate.of(2026, 1, 1).toEpochDay(),
            activeUntilEpochDay = null,
            isArchived = false,
            source = "manual"
        )

        assertTrue(calculator.matches(habit, LocalDate.of(2026, 4, 6))) // Monday
        assertFalse(calculator.matches(habit, LocalDate.of(2026, 4, 5))) // Sunday
    }

    @Test
    fun buildProfileAnalytics_appliesPersistedUnlockDate() {
        val createdAt = LocalDate.of(2026, 3, 1).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val habit = HabitEntity(
            id = 1,
            cloudId = "c1",
            title = "Habit",
            iconKey = "water",
            colorKey = "mint",
            frequencyType = "DAILY",
            customDaysCsv = "",
            reminderEnabled = true,
            reminderHour = 9,
            reminderMinute = 0,
            createdAt = createdAt,
            startEpochDay = LocalDate.of(2026, 3, 1).toEpochDay(),
            activeUntilEpochDay = null,
            isArchived = false,
            source = "manual"
        )

        val completions = (0 until 7).map { offset ->
            HabitCompletionEntity(
                habitId = 1,
                dateEpochDay = LocalDate.of(2026, 3, 1).plusDays(offset.toLong()).toEpochDay(),
                completedAtMillis = createdAt + offset * 86_400_000L
            )
        }

        val persistedUnlocks = listOf(
            AchievementUnlockEntity(
                achievementId = "week_7",
                unlockedEpochDay = LocalDate.of(2026, 3, 2).toEpochDay()
            )
        )

        val analytics = calculator.buildProfileAnalytics(
            habits = listOf(habit),
            completions = completions,
            persistedUnlocks = persistedUnlocks,
            today = LocalDate.of(2026, 3, 7)
        )

        val week7 = analytics.allAchievements.first { it.id == "week_7" }
        assertTrue(week7.unlocked)
        assertEquals(LocalDate.of(2026, 3, 2), week7.unlockedDate)
    }

    @Test
    fun buildStatsSnapshot_period7_builds7HeatmapCellsAndStartDate() {
        val today = LocalDate.of(2026, 4, 4)
        val habit = testHabit(id = 1, createdAtMillis = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000)

        val snapshot = calculator.buildStatsSnapshot(
            habits = listOf(habit),
            completions = emptyList(),
            periodDays = 7,
            today = today
        )

        assertEquals(7, snapshot.heatmapLevels.size)
        assertEquals(7, snapshot.heatmapCounts.size)
        assertEquals(today.minusDays(6).toEpochDay(), snapshot.heatmapStartEpochDay)
    }

    @Test
    fun buildStatsSnapshot_period90_builds90HeatmapCellsAndStartDate() {
        val today = LocalDate.of(2026, 4, 4)
        val habit = testHabit(id = 1, createdAtMillis = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000)

        val snapshot = calculator.buildStatsSnapshot(
            habits = listOf(habit),
            completions = emptyList(),
            periodDays = 90,
            today = today
        )

        assertEquals(90, snapshot.heatmapLevels.size)
        assertEquals(90, snapshot.heatmapCounts.size)
        assertEquals(today.minusDays(89).toEpochDay(), snapshot.heatmapStartEpochDay)
    }

    @Test
    fun buildStatsSnapshot_decouplesSummaryPeriodFromHeatmapPeriod() {
        val today = LocalDate.of(2026, 4, 4)
        val habit = testHabit(id = 1, createdAtMillis = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000)

        val snapshot = calculator.buildStatsSnapshot(
            habits = listOf(habit),
            completions = emptyList(),
            periodDays = 7,
            heatmapPeriodDays = 90,
            today = today
        )

        assertEquals(90, snapshot.heatmapLevels.size)
        assertEquals(90, snapshot.heatmapCounts.size)
        assertEquals(today.minusDays(89).toEpochDay(), snapshot.heatmapStartEpochDay)
    }

    private fun testHabit(id: Long, createdAtMillis: Long): HabitEntity = HabitEntity(
        id = id,
        cloudId = "c$id",
        title = "Habit $id",
        iconKey = "water",
        colorKey = "mint",
        frequencyType = "DAILY",
        customDaysCsv = "",
        reminderEnabled = true,
        reminderHour = 9,
        reminderMinute = 0,
        createdAt = createdAtMillis,
        startEpochDay = LocalDate.of(2026, 1, 1).toEpochDay(),
        activeUntilEpochDay = null,
        isArchived = false,
        source = "manual"
    )
}
