package com.vadymdev.habitix.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SyncSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SyncUserHabitsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase,
    private val syncSettingsUseCase: SyncSettingsUseCase
) : ViewModel() {

    companion object {
        private const val STEP_INITIAL = -1
        private const val STEP_CONNECTION = 0
        private const val STEP_PROFILE = 1
        private const val STEP_SETUP = 2
        private const val STEP_FINISH = 3
        private const val STEP_ALL_COMPLETED = 4
    }

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isAuthorized = false,
                    showLoadingFlow = true,
                    loadingStepIndex = STEP_INITIAL
                )
            }

            val result = signInWithGoogleUseCase(idToken)
            result.onSuccess { session ->
                runPostAuthSequence(session)
            }.onFailure { error ->
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        error = error.message ?: "Не вдалося увійти через Google",
                        showLoadingFlow = false,
                        loadingStepIndex = -1
                    )
                }
            }
        }
    }

    fun setError(message: String?) {
        _state.update { it.copy(error = message) }
    }

    fun continueAsGuest(onDone: () -> Unit) {
        viewModelScope.launch {
            continueAsGuestUseCase()
            onDone()
        }
    }

    private suspend fun runPostAuthSequence(session: UserSession) {
        delay(260)
        _state.update { it.copy(loadingStepIndex = STEP_CONNECTION) }
        delay(700)
        _state.update { it.copy(loadingStepIndex = STEP_PROFILE) }
        delay(700)
        _state.update { it.copy(loadingStepIndex = STEP_SETUP) }
        runCatching { syncUserHabitsUseCase(session.uid) }
        runCatching { syncSettingsUseCase(session.uid) }
        delay(700)
        _state.update { it.copy(loadingStepIndex = STEP_FINISH) }
        delay(700)
        _state.update { it.copy(loadingStepIndex = STEP_ALL_COMPLETED) }
        delay(600)
        _state.update {
            it.copy(
                isLoading = false,
                isAuthorized = true,
                showLoadingFlow = false,
                loadingStepIndex = -1
            )
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthorized: Boolean = false,
    val showLoadingFlow: Boolean = false,
    val loadingStepIndex: Int = -1
)

class AuthViewModelFactory(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase,
    private val syncUserHabitsUseCase: SyncUserHabitsUseCase,
    private val syncSettingsUseCase: SyncSettingsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                signInWithGoogleUseCase = signInWithGoogleUseCase,
                continueAsGuestUseCase = continueAsGuestUseCase,
                syncUserHabitsUseCase = syncUserHabitsUseCase,
                syncSettingsUseCase = syncSettingsUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
