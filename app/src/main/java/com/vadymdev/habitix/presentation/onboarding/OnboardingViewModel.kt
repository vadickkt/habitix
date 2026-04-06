package com.vadymdev.habitix.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.HabitTemplate
import com.vadymdev.habitix.domain.model.InterestCategory
import com.vadymdev.habitix.domain.usecase.CompleteOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.domain.usecase.UpdateHabitsUseCase
import com.vadymdev.habitix.domain.usecase.UpdateInterestsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    observeOnboardingUseCase: ObserveOnboardingUseCase,
    private val updateInterestsUseCase: UpdateInterestsUseCase,
    private val updateHabitsUseCase: UpdateHabitsUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()
    private var completionInProgress: Boolean = false

    init {
        viewModelScope.launch {
            observeOnboardingUseCase().collect { data ->
                _state.update {
                    it.copy(
                        selectedInterestKeys = data.selectedInterests,
                        selectedHabitKeys = data.selectedHabits,
                        isCompleted = data.completed
                    )
                }
            }
        }
    }

    fun toggleInterest(key: String) {
        val current = _state.value.selectedInterestKeys.toMutableSet()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }

        _state.update { it.copy(selectedInterestKeys = current) }

        viewModelScope.launch {
            updateInterestsUseCase(current)
        }
    }

    fun toggleHabit(key: String) {
        val current = _state.value.selectedHabitKeys.toMutableSet()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }

        _state.update { it.copy(selectedHabitKeys = current) }

        viewModelScope.launch {
            updateHabitsUseCase(current)
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        if (completionInProgress) return
        completionInProgress = true

        viewModelScope.launch {
            runCatching {
                completeOnboardingUseCase(_state.value.selectedHabitKeys)
            }.onSuccess {
                onDone()
            }.also {
                completionInProgress = false
            }
        }
    }
}

data class OnboardingUiState(
    val selectedInterestKeys: Set<String> = emptySet(),
    val selectedHabitKeys: Set<String> = emptySet(),
    val isCompleted: Boolean = false,
    val interests: List<InterestCategory> = listOf(
        InterestCategory("health", "Здоров'я", "❤", 0xFFF8D2DC),
        InterestCategory("productivity", "Продуктивність", "⚡", 0xFFFFD7A8),
        InterestCategory("sport", "Спорт", "◎", 0xFFB8F3D6),
        InterestCategory("mindfulness", "Усвідомленість", "✧", 0xFFD6CBFF)
    ),
    val habits: List<HabitTemplate> = listOf(
        HabitTemplate("water", "Пити воду", "💧"),
        HabitTemplate("meditation", "Медитація", "🧘"),
        HabitTemplate("morning", "Ранкова зарядка", "🏃"),
        HabitTemplate("reading", "Читання", "📚"),
        HabitTemplate("sleep", "Сон до 23:00", "😴"),
        HabitTemplate("gratitude", "Вдячність", "🙏")
    )
)
