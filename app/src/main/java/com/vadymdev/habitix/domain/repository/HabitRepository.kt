package com.vadymdev.habitix.domain.repository

import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun observeHabitsForDate(date: LocalDate): Flow<List<Habit>>
    fun observeStats(periodDays: Int): Flow<HabitStatsSnapshot>
    fun observeProfileAnalytics(): Flow<ProfileAnalytics>
    suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate, completed: Boolean)
    suspend fun updateHabit(habitId: Long, draft: HabitCreateDraft)
    suspend fun hideHabitForDate(habitId: Long, date: LocalDate)
    suspend fun deactivateHabitFromDate(habitId: Long, date: LocalDate)
    suspend fun deleteAllHabits()
    suspend fun createHabit(draft: HabitCreateDraft)
    suspend fun seedOnboardingHabits(habitKeys: Set<String>)
    suspend fun getIncompleteHabitsForDate(date: LocalDate): List<Habit>
}
