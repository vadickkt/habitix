package com.vadymdev.habitix.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.usecase.ContinueAsGuestUseCase
import com.vadymdev.habitix.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signInWithGoogleUseCase(idToken)
            result.onSuccess {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        error = null,
                        isAuthorized = true
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Не вдалося увійти через Google"
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
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthorized: Boolean = false
)

class AuthViewModelFactory(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                signInWithGoogleUseCase = signInWithGoogleUseCase,
                continueAsGuestUseCase = continueAsGuestUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
