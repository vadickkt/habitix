package com.vadymdev.habitix.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vadymdev.habitix.domain.model.ProfileAchievement
import com.vadymdev.habitix.domain.model.ProfileAnalytics
import com.vadymdev.habitix.domain.model.ProfileIdentity
import com.vadymdev.habitix.domain.usecase.ObserveAuthSessionUseCase
import com.vadymdev.habitix.domain.usecase.ObserveProfileAnalyticsUseCase
import com.vadymdev.habitix.domain.usecase.ObserveProfileIdentityUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.domain.usecase.SyncScope
import com.vadymdev.habitix.domain.usecase.UpdateProfileBioUseCase
import com.vadymdev.habitix.domain.usecase.UpdateProfileAvatarUseCase
import com.vadymdev.habitix.domain.usecase.UpdateProfileNameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    observeProfileIdentityUseCase: ObserveProfileIdentityUseCase,
    observeProfileAnalyticsUseCase: ObserveProfileAnalyticsUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val syncOrchestratorUseCase: SyncOrchestratorUseCase,
    private val updateProfileNameUseCase: UpdateProfileNameUseCase,
    private val updateProfileBioUseCase: UpdateProfileBioUseCase,
    private val updateProfileAvatarUseCase: UpdateProfileAvatarUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val selectedCategory = MutableStateFlow(ACHIEVEMENTS_CATEGORY_ALL)
    private val currentUserId = MutableStateFlow<String?>(null)
    private val avatarUpdating = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            observeAuthSessionUseCase().collect { session ->
                currentUserId.value = session?.uid
                session?.uid?.let { uid ->
                    syncOrchestratorUseCase(uid, SyncScope.PROFILE_ONLY)
                        .onFailure { Log.w(TAG, "Profile sync failed on auth session change", it) }
                }
            }
        }
    }

    val state: StateFlow<ProfileUiState> = combine(
        observeProfileIdentityUseCase(),
        observeProfileAnalyticsUseCase(),
        observeAuthSessionUseCase(),
        selectedCategory,
        avatarUpdating
    ) { identity, analytics, session, category, isAvatarUpdating ->
        val resolvedName = if (identity.displayName == "Користувач") {
            session?.displayName ?: identity.displayName
        } else {
            identity.displayName
        }

        val normalizedIdentity = identity.copy(
            displayName = resolvedName,
            avatarInitials = initialsFor(resolvedName)
        )

        val filteredAchievements = if (category == ACHIEVEMENTS_CATEGORY_ALL) {
            analytics.allAchievements
        } else {
            analytics.allAchievements.filter { it.category == category }
        }

        ProfileUiState(
            identity = normalizedIdentity,
            analytics = analytics,
            selectedCategory = category,
            achievements = filteredAchievements,
            unlockedCount = analytics.allAchievements.count { it.unlocked },
            isAvatarUpdating = isAvatarUpdating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun updateName(value: String) {
        viewModelScope.launch {
            updateProfileNameUseCase(value)
            currentUserId.value?.let { uid ->
                syncOrchestratorUseCase(uid, SyncScope.PROFILE_ONLY)
                    .onFailure { Log.w(TAG, "Profile sync failed after name update", it) }
            }
        }
    }

    fun updateBio(value: String) {
        viewModelScope.launch {
            updateProfileBioUseCase(value)
            currentUserId.value?.let { uid ->
                syncOrchestratorUseCase(uid, SyncScope.PROFILE_ONLY)
                    .onFailure { Log.w(TAG, "Profile sync failed after bio update", it) }
            }
        }
    }

    fun updateAvatar(uri: String?) {
        viewModelScope.launch {
            avatarUpdating.value = true
            try {
                updateProfileAvatarUseCase(uri)
                currentUserId.value?.let { uid ->
                    syncOrchestratorUseCase(uid, SyncScope.PROFILE_ONLY)
                        .onFailure { Log.w(TAG, "Profile sync failed after avatar update", it) }
                }
            } finally {
                avatarUpdating.value = false
            }
        }
    }

    fun setAchievementCategory(value: String) {
        selectedCategory.value = value
    }

    private fun initialsFor(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "HU"
            parts.size == 1 -> parts.first().take(2).uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }
}

data class ProfileUiState(
    val identity: ProfileIdentity = ProfileIdentity(
        displayName = "Користувач",
        bio = "Будую кращу версію себе",
        avatarInitials = "HU",
        avatarUri = null,
        updatedAtMillis = 0L
    ),
    val analytics: ProfileAnalytics = ProfileAnalytics(
        level = 1,
        xpCurrent = 0,
        xpTarget = 1000,
        currentStreakDays = 0,
        bestStreakDays = 0,
        totalCompleted = 0,
        daysWithUs = 0,
        monthGrowthPercent = 0,
        monthWeeklyActivity = listOf(0, 0, 0, 0),
        topAchievements = emptyList(),
        allAchievements = emptyList()
    ),
    val selectedCategory: String = ACHIEVEMENTS_CATEGORY_ALL,
    val achievements: List<ProfileAchievement> = emptyList(),
    val unlockedCount: Int = 0,
    val isAvatarUpdating: Boolean = false
)

class ProfileViewModelFactory(
    private val observeProfileIdentityUseCase: ObserveProfileIdentityUseCase,
    private val observeProfileAnalyticsUseCase: ObserveProfileAnalyticsUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val syncOrchestratorUseCase: SyncOrchestratorUseCase,
    private val updateProfileNameUseCase: UpdateProfileNameUseCase,
    private val updateProfileBioUseCase: UpdateProfileBioUseCase,
    private val updateProfileAvatarUseCase: UpdateProfileAvatarUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                observeProfileIdentityUseCase = observeProfileIdentityUseCase,
                observeProfileAnalyticsUseCase = observeProfileAnalyticsUseCase,
                observeAuthSessionUseCase = observeAuthSessionUseCase,
                syncOrchestratorUseCase = syncOrchestratorUseCase,
                updateProfileNameUseCase = updateProfileNameUseCase,
                updateProfileBioUseCase = updateProfileBioUseCase,
                updateProfileAvatarUseCase = updateProfileAvatarUseCase
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
