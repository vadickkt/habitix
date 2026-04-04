package com.vadymdev.habitix.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

data class StartupState(
    val pushEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val autoSyncEnabled: Boolean = false,
    val hasAuthorizedUser: Boolean = false
)

class StartupViewModel(
    observeSettingsUseCase: ObserveSettingsUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase
) : ViewModel() {

    val startupState: StateFlow<StartupState> = combine(
        observeSettingsUseCase(),
        observeAuthSessionUseCase()
    ) { settings, session ->
        StartupState(
            pushEnabled = settings.pushEnabled,
            reminderHour = settings.reminderHour,
            reminderMinute = settings.reminderMinute,
            autoSyncEnabled = settings.autoSyncEnabled,
            hasAuthorizedUser = session != null
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StartupState()
    )
}
