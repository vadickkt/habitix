package com.vadymdev.habitix.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveGuestModeUseCase
import com.vadymdev.habitix.domain.usecase.ObserveOnboardingUseCase
import com.vadymdev.habitix.presentation.navigation.AppRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    observeOnboardingUseCase: ObserveOnboardingUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    observeGuestModeUseCase: ObserveGuestModeUseCase
) : ViewModel() {

    val startDestination: StateFlow<String> = combine(
        observeOnboardingUseCase(),
        observeAuthSessionUseCase(),
        observeGuestModeUseCase()
    ) { onboarding, session, isGuest ->
        when {
            !onboarding.completed -> AppRoute.OnboardingIntro
            session == null && !isGuest -> AppRoute.Auth
            else -> AppRoute.Dashboard
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppRoute.Loading
    )
}
