package com.vadymdev.habitix.presentation.settings

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.domain.usecase.DeleteAccountUseCase
import com.vadymdev.habitix.domain.usecase.DeleteDataUseCase
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveSettingsUseCase
import com.vadymdev.habitix.domain.usecase.SetAccentPaletteUseCase
import com.vadymdev.habitix.domain.usecase.SetAutoSyncEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetBiometricEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetLanguageUseCase
import com.vadymdev.habitix.domain.usecase.SetPushEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetReminderTimeUseCase
import com.vadymdev.habitix.domain.usecase.SetSoundsEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SetThemeModeUseCase
import com.vadymdev.habitix.domain.usecase.SetVibrationEnabledUseCase
import com.vadymdev.habitix.domain.usecase.SignOutUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.domain.usecase.SyncScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeSettingsUseCase: ObserveSettingsUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAccentPaletteUseCase: SetAccentPaletteUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setPushEnabledUseCase: SetPushEnabledUseCase,
    private val setReminderTimeUseCase: SetReminderTimeUseCase,
    private val setSoundsEnabledUseCase: SetSoundsEnabledUseCase,
    private val setVibrationEnabledUseCase: SetVibrationEnabledUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val setAutoSyncEnabledUseCase: SetAutoSyncEnabledUseCase,
    private val syncOrchestratorUseCase: SyncOrchestratorUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val deleteDataUseCase: DeleteDataUseCase,
    private val languageApplier: (AppLanguage) -> Unit = { language ->
        val tag = when (language) {
            AppLanguage.UK -> "uk"
            AppLanguage.EN -> "en"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
) : ViewModel() {

    private companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeSettingsUseCase(),
                observeAuthSessionUseCase()
            ) { settings, session ->
                settings to session?.uid
            }.collect { (settings, uid) ->
                applyLanguage(settings.language)
                _state.update {
                    it.copy(
                        settings = settings,
                        userId = uid
                    )
                }
            }
        }
    }

    fun setThemeMode(value: ThemeMode) = updateAndSync { setThemeModeUseCase(value) }
    fun setAccentPalette(value: AccentPalette) = updateAndSync { setAccentPaletteUseCase(value) }

    fun setLanguage(value: AppLanguage) {
        viewModelScope.launch {
            setLanguageUseCase(value)
            applyLanguage(value)
            syncIfNeeded()
        }
    }

    fun setPushEnabled(value: Boolean) = updateAndSync { setPushEnabledUseCase(value) }
    fun setReminderTime(hour: Int, minute: Int) = updateAndSync { setReminderTimeUseCase(hour, minute) }
    fun setSoundsEnabled(value: Boolean) = updateAndSync { setSoundsEnabledUseCase(value) }
    fun setVibrationEnabled(value: Boolean) = updateAndSync { setVibrationEnabledUseCase(value) }
    fun setBiometricEnabled(value: Boolean) = updateAndSync { setBiometricEnabledUseCase(value) }

    fun setAutoSyncEnabled(value: Boolean) {
        viewModelScope.launch {
            setAutoSyncEnabledUseCase(value)
            if (value) {
                val uid = _state.value.userId
                if (uid != null) {
                    syncOrchestratorUseCase(uid, SyncScope.SETTINGS_ONLY)
                        .onFailure { Log.w(TAG, "Settings sync failed after auto-sync enabled", it) }
                }
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onDone()
        }
    }

    fun deleteAccount(onDone: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            deleteAccountUseCase()
                .onSuccess { onDone() }
                .onFailure { onError(it.message ?: "Не вдалося видалити акаунт") }
        }
    }

    fun deleteData() {
        if (_state.value.deleteData.phase == DeleteDataPhase.RUNNING) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    deleteData = DeleteDataUiState(
                        phase = DeleteDataPhase.RUNNING,
                        stepIndex = 0,
                        errorMessage = null
                    )
                )
            }

            runCatching {
                _state.update { state -> state.copy(deleteData = state.deleteData.copy(stepIndex = 1)) }
                deleteDataUseCase(_state.value.userId)
                _state.update { state -> state.copy(deleteData = state.deleteData.copy(stepIndex = 2)) }
            }.onSuccess {
                _state.update {
                    it.copy(
                        deleteData = DeleteDataUiState(
                            phase = DeleteDataPhase.SUCCESS,
                            stepIndex = 3,
                            errorMessage = null
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        deleteData = DeleteDataUiState(
                            phase = DeleteDataPhase.ERROR,
                            stepIndex = it.deleteData.stepIndex,
                            errorMessage = error.message ?: "Не вдалося видалити дані"
                        )
                    )
                }
            }
        }
    }

    fun dismissDeleteDataState() {
        _state.update { it.copy(deleteData = DeleteDataUiState()) }
    }

    suspend fun syncNow() {
        syncIfNeeded()
    }

    private fun updateAndSync(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
            syncIfNeeded()
        }
    }

    private suspend fun syncIfNeeded() {
        val snapshot = _state.value
        val uid = snapshot.userId ?: return
        if (!snapshot.settings.autoSyncEnabled) return
        syncOrchestratorUseCase(uid, SyncScope.SETTINGS_ONLY)
            .onFailure { Log.w(TAG, "Settings sync failed", it) }
    }

    private fun applyLanguage(language: AppLanguage) {
        languageApplier(language)
    }
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val userId: String? = null,
    val deleteData: DeleteDataUiState = DeleteDataUiState()
)

enum class DeleteDataPhase {
    IDLE,
    RUNNING,
    SUCCESS,
    ERROR
}

data class DeleteDataUiState(
    val phase: DeleteDataPhase = DeleteDataPhase.IDLE,
    val stepIndex: Int = 0,
    val errorMessage: String? = null
)

class SettingsViewModelFactory(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAccentPaletteUseCase: SetAccentPaletteUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setPushEnabledUseCase: SetPushEnabledUseCase,
    private val setReminderTimeUseCase: SetReminderTimeUseCase,
    private val setSoundsEnabledUseCase: SetSoundsEnabledUseCase,
    private val setVibrationEnabledUseCase: SetVibrationEnabledUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val setAutoSyncEnabledUseCase: SetAutoSyncEnabledUseCase,
    private val syncOrchestratorUseCase: SyncOrchestratorUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val deleteDataUseCase: DeleteDataUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                observeSettingsUseCase = observeSettingsUseCase,
                observeAuthSessionUseCase = observeAuthSessionUseCase,
                setThemeModeUseCase = setThemeModeUseCase,
                setAccentPaletteUseCase = setAccentPaletteUseCase,
                setLanguageUseCase = setLanguageUseCase,
                setPushEnabledUseCase = setPushEnabledUseCase,
                setReminderTimeUseCase = setReminderTimeUseCase,
                setSoundsEnabledUseCase = setSoundsEnabledUseCase,
                setVibrationEnabledUseCase = setVibrationEnabledUseCase,
                setBiometricEnabledUseCase = setBiometricEnabledUseCase,
                setAutoSyncEnabledUseCase = setAutoSyncEnabledUseCase,
                syncOrchestratorUseCase = syncOrchestratorUseCase,
                signOutUseCase = signOutUseCase,
                deleteAccountUseCase = deleteAccountUseCase,
                deleteDataUseCase = deleteDataUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
