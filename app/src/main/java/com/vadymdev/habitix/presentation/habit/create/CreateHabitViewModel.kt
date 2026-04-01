package com.vadymdev.habitix.presentation.habit.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.domain.usecase.CreateHabitUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class CreateHabitViewModel(
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitUiState())
    val state: StateFlow<CreateHabitUiState> = _state.asStateFlow()

    fun setTitle(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun setIcon(key: String) {
        _state.update { it.copy(selectedIconKey = key) }
    }

    fun setColor(key: String) {
        _state.update { it.copy(selectedColorKey = key) }
    }

    fun setFrequency(value: HabitFrequencyType) {
        _state.update { state ->
            val days = if (value == HabitFrequencyType.CUSTOM) state.customDays else emptySet()
            state.copy(frequency = value, customDays = days)
        }
    }

    fun toggleCustomDay(dayOfWeek: DayOfWeek) {
        val current = _state.value.customDays.toMutableSet()
        if (current.contains(dayOfWeek)) current.remove(dayOfWeek) else current.add(dayOfWeek)
        _state.update { it.copy(customDays = current) }
    }

    fun toggleReminder() {
        _state.update { it.copy(reminderEnabled = !it.reminderEnabled) }
    }

    fun createHabit(onCreated: () -> Unit) {
        val snapshot = _state.value
        if (snapshot.title.isBlank()) return
        if (snapshot.frequency == HabitFrequencyType.CUSTOM && snapshot.customDays.isEmpty()) return

        viewModelScope.launch {
            createHabitUseCase(
                HabitCreateDraft(
                    title = snapshot.title.trim(),
                    iconKey = snapshot.selectedIconKey,
                    colorKey = snapshot.selectedColorKey,
                    frequencyType = snapshot.frequency,
                    customDays = snapshot.customDays,
                    reminderEnabled = snapshot.reminderEnabled
                )
            )
            onCreated()
        }
    }
}

data class CreateHabitUiState(
    val title: String = "",
    val selectedIconKey: String = "water",
    val selectedColorKey: String = "mint",
    val frequency: HabitFrequencyType = HabitFrequencyType.DAILY,
    val customDays: Set<DayOfWeek> = emptySet(),
    val reminderEnabled: Boolean = true
)

class CreateHabitViewModelFactory(
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateHabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateHabitViewModel(createHabitUseCase = createHabitUseCase) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
