package com.vadymdev.habitix.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.presentation.navigation.AppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class AppViewModel(
    observeOnboardingUseCase: ObserveOnboardingUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase
) : ViewModel() {

    val startDestination: StateFlow<String> = combine(
        observeOnboardingUseCase(),
        observeAuthSessionUseCase()
    ) { onboarding, session ->
        when {
            !onboarding.completed -> AppRoute.OnboardingIntro
            session == null -> AppRoute.Auth
            else -> AppRoute.Dashboard
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppRoute.Loading
    )
}

class AppViewModelFactory(
    private val observeOnboardingUseCase: ObserveOnboardingUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(
                observeOnboardingUseCase = observeOnboardingUseCase,
                observeAuthSessionUseCase = observeAuthSessionUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
