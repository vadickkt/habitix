package com.vadymdev.habitix.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.usecase.ObserveHabitsForDateUseCase
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
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val habitsForDate = selectedDate
        .flatMapLatest { date -> observeHabitsForDateUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<DashboardUiState> = combine(selectedDate, habitsForDate) { date, habits ->
        DashboardUiState(
            selectedDate = date,
            habits = habits,
            completedCount = habits.count { it.isCompletedForSelectedDate },
            totalCount = habits.size
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
        }
    }
}

data class DashboardUiState(
    val selectedDate: LocalDate,
    val habits: List<Habit> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0
)

class DashboardViewModelFactory(
    private val observeHabitsForDateUseCase: ObserveHabitsForDateUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                observeHabitsForDateUseCase = observeHabitsForDateUseCase,
                toggleHabitCompletionUseCase = toggleHabitCompletionUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
