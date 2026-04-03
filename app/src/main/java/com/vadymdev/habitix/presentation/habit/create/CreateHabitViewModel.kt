package com.vadymdev.habitix.presentation.habit.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.HabitCreateDraft
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.domain.usecase.CreateHabitUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitUseCase
import com.vadymdev.habitix.domain.usecase.ValidateHabitTitleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class CreateHabitViewModel(
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val validateHabitTitleUseCase: ValidateHabitTitleUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitUiState())
    val state: StateFlow<CreateHabitUiState> = _state.asStateFlow()
    private val currentUserId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            observeAuthSessionUseCase().collect { session ->
                currentUserId.value = session?.uid
            }
        }
    }

    fun setTitle(value: String) {
        _state.update { it.copy(title = value, titleError = null, daysError = null) }
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
            state.copy(frequency = value, customDays = days, daysError = null)
        }
    }

    fun toggleCustomDay(dayOfWeek: DayOfWeek) {
        val current = _state.value.customDays.toMutableSet()
        if (current.contains(dayOfWeek)) current.remove(dayOfWeek) else current.add(dayOfWeek)
        _state.update { it.copy(customDays = current, daysError = null) }
    }

    fun toggleReminder() {
        _state.update { it.copy(reminderEnabled = !it.reminderEnabled) }
    }

    fun startEditing(habit: Habit) {
        _state.value = CreateHabitUiState(
            editingHabitId = habit.id,
            title = habit.title,
            selectedIconKey = habit.iconKey,
            selectedColorKey = habit.colorKey,
            frequency = habit.frequencyType,
            customDays = habit.customDays,
            reminderEnabled = habit.reminderEnabled
        )
    }

    fun resetDraft() {
        _state.value = CreateHabitUiState()
    }

    fun saveHabit(onSaved: () -> Unit) {
        val snapshot = _state.value
        val titleValidation = validateHabitTitleUseCase(snapshot.title)
        val daysError = if (snapshot.frequency == HabitFrequencyType.CUSTOM && snapshot.customDays.isEmpty()) {
            "Оберіть принаймні 1 день"
        } else {
            null
        }

        if (!titleValidation.isValid || daysError != null) {
            _state.update {
                it.copy(
                    titleError = titleValidation.errorMessage,
                    daysError = daysError
                )
            }
            return
        }

        val draft = HabitCreateDraft(
            title = snapshot.title.trim(),
            iconKey = snapshot.selectedIconKey,
            colorKey = snapshot.selectedColorKey,
            frequencyType = snapshot.frequency,
            customDays = snapshot.customDays,
            reminderEnabled = snapshot.reminderEnabled
        )

        viewModelScope.launch {
            val editingId = snapshot.editingHabitId
            if (editingId == null) {
                createHabitUseCase(draft)
            } else {
                updateHabitUseCase(editingId, draft)
            }

            currentUserId.value?.let { uid ->
                runCatching { syncUserHabitsUseCase(uid) }
            }
            resetDraft()
            onSaved()
        }
    }
}

data class CreateHabitUiState(
    val editingHabitId: Long? = null,
    val title: String = "",
    val titleError: String? = null,
    val selectedIconKey: String = "water",
    val selectedColorKey: String = "mint",
    val frequency: HabitFrequencyType = HabitFrequencyType.DAILY,
    val customDays: Set<DayOfWeek> = emptySet(),
    val daysError: String? = null,
    val reminderEnabled: Boolean = true
)

class CreateHabitViewModelFactory(
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val validateHabitTitleUseCase: ValidateHabitTitleUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateHabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateHabitViewModel(
                createHabitUseCase = createHabitUseCase,
                updateHabitUseCase = updateHabitUseCase,
                validateHabitTitleUseCase = validateHabitTitleUseCase,
                observeAuthSessionUseCase = observeAuthSessionUseCase,
                syncUserHabitsUseCase = syncUserHabitsUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
