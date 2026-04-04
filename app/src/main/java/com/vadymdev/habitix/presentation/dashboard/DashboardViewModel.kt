package com.vadymdev.habitix.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.usecase.DeactivateHabitFromDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.ObserveProfileAnalyticsUseCase
import com.vadymdev.habitix.domain.usecase.SyncAchievementsUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import com.vadymdev.habitix.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val observeHabitsForDateUseCase: ObserveHabitsForDateUseCase,
    private val observeProfileAnalyticsUseCase: ObserveProfileAnalyticsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val deactivateHabitFromDateUseCase: DeactivateHabitFromDateUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase,
    private val syncAchievementsUseCase: SyncAchievementsUseCase
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val currentUserId = MutableStateFlow<String?>(null)
    private val unlockEvent = MutableStateFlow<DashboardAchievementEvent?>(null)
    private var unlockSequence = 0L
    private var initializedUnlockedSnapshot = false
    private val knownUnlockedIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            observeAuthSessionUseCase().collect { session ->
                currentUserId.value = session?.uid
            }
        }

        viewModelScope.launch {
            observeProfileAnalyticsUseCase().collect { analytics ->
                val unlockedNow = analytics.allAchievements
                    .filter { it.unlocked }
                    .associateBy { it.id }

                if (!initializedUnlockedSnapshot) {
                    knownUnlockedIds.addAll(unlockedNow.keys)
                    initializedUnlockedSnapshot = true
                    return@collect
                }

                val newUnlocked = unlockedNow.keys.filterNot { knownUnlockedIds.contains(it) }
                if (newUnlocked.isNotEmpty()) {
                    val first = unlockedNow.getValue(newUnlocked.first())
                    unlockSequence += 1
                    unlockEvent.value = DashboardAchievementEvent(
                        id = unlockSequence,
                        title = first.title,
                        xpReward = first.xpReward
                    )
                    knownUnlockedIds.addAll(newUnlocked)
                }
            }
        }
    }

    private val habitsForDate = selectedDate
        .flatMapLatest { date -> observeHabitsForDateUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<DashboardUiState> = combine(selectedDate, habitsForDate, unlockEvent) { date, habits, event ->
        DashboardUiState(
            selectedDate = date,
            habits = habits,
            completedCount = habits.count { it.isCompletedForSelectedDate },
            totalCount = habits.size,
            achievementEvent = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(selectedDate = LocalDate.now())
    )

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onToggleHabit(habit: Habit) {
        viewModelScope.launch {
            toggleHabitCompletionUseCase(
                habitId = habit.id,
                date = state.value.selectedDate,
                completed = !habit.isCompletedForSelectedDate
            )
            syncIfAuthorized()
        }
    }

    fun deleteHabitFromToday(habit: Habit) {
        viewModelScope.launch {
            deactivateHabitFromDateUseCase(habit.id, state.value.selectedDate)
            syncIfAuthorized()
        }
    }

    fun consumeAchievementEvent(eventId: Long) {
        if (unlockEvent.value?.id == eventId) {
            unlockEvent.value = null
        }
    }

    private suspend fun syncIfAuthorized() {
        currentUserId.value?.let { uid ->
            runCatching { syncUserHabitsUseCase(uid) }
            runCatching { syncAchievementsUseCase(uid) }
        }
    }
}

data class DashboardUiState(
    val selectedDate: LocalDate,
    val habits: List<Habit> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val achievementEvent: DashboardAchievementEvent? = null
)

data class DashboardAchievementEvent(
    val id: Long,
    val title: String,
    val xpReward: Int
)

class DashboardViewModelFactory(
    private val observeHabitsForDateUseCase: ObserveHabitsForDateUseCase,
    private val observeProfileAnalyticsUseCase: ObserveProfileAnalyticsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val deactivateHabitFromDateUseCase: DeactivateHabitFromDateUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase,
    private val syncAchievementsUseCase: SyncAchievementsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                observeHabitsForDateUseCase = observeHabitsForDateUseCase,
                observeProfileAnalyticsUseCase = observeProfileAnalyticsUseCase,
                toggleHabitCompletionUseCase = toggleHabitCompletionUseCase,
                deactivateHabitFromDateUseCase = deactivateHabitFromDateUseCase,
                observeAuthSessionUseCase = observeAuthSessionUseCase,
                syncUserHabitsUseCase = syncUserHabitsUseCase,
                syncAchievementsUseCase = syncAchievementsUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
