package com.vadymdev.habitix.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vadymdev.habitix.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore by preferencesDataStore(name = "habitix_prefs")

class OnboardingPreferencesDataSource(private val context: Context) {

    private val completedKey = booleanPreferencesKey("onboarding_completed")
    private val interestsKey = stringSetPreferencesKey("onboarding_interests")
    private val habitsKey = stringSetPreferencesKey("onboarding_habits")

    fun observeOnboardingState(): Flow<OnboardingState> = context.userPrefsDataStore.data.map { prefs ->
        OnboardingState(
            completed = prefs[completedKey] == true,
            selectedInterests = prefs[interestsKey] ?: emptySet(),
            selectedHabits = prefs[habitsKey] ?: emptySet()
        )
    }

    suspend fun updateInterests(values: Set<String>) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[interestsKey] = values
        }
    }

    suspend fun updateHabits(values: Set<String>) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[habitsKey] = values
        }
    }

    suspend fun setCompleted() {
        context.userPrefsDataStore.edit { prefs ->
            prefs[completedKey] = true
        }
    }
}
