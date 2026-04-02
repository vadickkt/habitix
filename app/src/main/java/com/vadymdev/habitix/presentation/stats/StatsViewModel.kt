package com.vadymdev.habitix.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.domain.usecase.ObserveStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    observeStatsUseCase: ObserveStatsUseCase
) : ViewModel() {

    private val selectedPeriodDays = MutableStateFlow(30)
    private val selectedMetric = MutableStateFlow<StatsMetric?>(null)
    private val selectedCategory = MutableStateFlow<HabitCategoryStat?>(null)
    private val selectedBadge = MutableStateFlow<HabitBadge?>(null)
    private val selectedHeatmapIndex = MutableStateFlow<Int?>(null)

    private val snapshotFlow = selectedPeriodDays
        .flatMapLatest { days -> observeStatsUseCase(days) }

    val state: StateFlow<StatsUiState> = combine(
        snapshotFlow,
        selectedPeriodDays,
        selectedMetric,
        selectedCategory,
        selectedBadge,
        selectedHeatmapIndex
    ) { values ->
        val snapshot = values[0] as HabitStatsSnapshot
        val period = values[1] as Int
        val metric = values[2] as StatsMetric?
        val category = values[3] as HabitCategoryStat?
        val badge = values[4] as HabitBadge?
        val heatmapIndex = values[5] as Int?

        val dayDetails = heatmapIndex?.let { index ->
            val count = snapshot.heatmapCounts.getOrElse(index) { 0 }
            val date = LocalDate.ofEpochDay(snapshot.heatmapStartEpochDay + index)
            StatsHeatmapDayDetails(date = date, completedCount = count)
        }

        StatsUiState(
            snapshot = snapshot,
            selectedPeriodDays = period,
            selectedMetric = metric,
            selectedCategory = category,
            selectedBadge = badge,
            selectedHeatmapDay = dayDetails
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState()
        )

    fun setPeriod(days: Int) {
        selectedPeriodDays.value = days
    }

    fun openMetric(metric: StatsMetric) {
        selectedMetric.value = metric
    }

    fun closeMetric() {
        selectedMetric.value = null
    }

    fun openCategory(category: HabitCategoryStat) {
        selectedCategory.value = category
    }

    fun closeCategory() {
        selectedCategory.value = null
    }

    fun openBadge(badge: HabitBadge) {
        selectedBadge.value = badge
    }

    fun closeBadge() {
        selectedBadge.value = null
    }

    fun openHeatmapDay(index: Int) {
        selectedHeatmapIndex.value = index
    }

    fun closeHeatmapDay() {
        selectedHeatmapIndex.value = null
    }
}

data class StatsUiState(
    val selectedPeriodDays: Int = 30,
    val selectedMetric: StatsMetric? = null,
    val selectedCategory: HabitCategoryStat? = null,
    val selectedBadge: HabitBadge? = null,
    val selectedHeatmapDay: StatsHeatmapDayDetails? = null,
    val snapshot: HabitStatsSnapshot = HabitStatsSnapshot(
        longestStreak = 0,
        earnedBadgesCount = 0,
        successRatePercent = 0,
        completedTasksCount = 0,
        heatmapLevels = List(15 * 7) { 0 },
        heatmapCounts = List(15 * 7) { 0 },
        heatmapStartEpochDay = LocalDate.now().minusDays((15 * 7 - 1).toLong()).toEpochDay(),
        categoryStats = emptyList(),
        badges = emptyList()
    )
)

data class StatsHeatmapDayDetails(
    val date: LocalDate,
    val completedCount: Int
)

enum class StatsMetric {
    LONGEST_STREAK,
    BADGES,
    SUCCESS,
    COMPLETED
}

class StatsViewModelFactory(
    private val observeStatsUseCase: ObserveStatsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(observeStatsUseCase = observeStatsUseCase) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
