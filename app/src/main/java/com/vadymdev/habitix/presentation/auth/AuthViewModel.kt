package com.vadymdev.habitix.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.UserSession
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.domain.usecase.SyncScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase,
    private val syncOrchestratorUseCase: SyncOrchestratorUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
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
                    promoteGuestFallback = false,
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
                        promoteGuestFallback = true,
                        showLoadingFlow = false,
                        loadingStepIndex = -1
                    )
                }
            }
        }
    }

    fun setError(message: String?, promoteGuestFallback: Boolean = false) {
        _state.update {
            it.copy(
                error = message,
                promoteGuestFallback = promoteGuestFallback
            )
        }
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
        syncOrchestratorUseCase(userId = session.uid, scope = SyncScope.FULL)
            .onFailure { Log.w(TAG, "Post-auth sync failed", it) }
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
    val promoteGuestFallback: Boolean = false,
    val isAuthorized: Boolean = false,
    val showLoadingFlow: Boolean = false,
    val loadingStepIndex: Int = -1
)
