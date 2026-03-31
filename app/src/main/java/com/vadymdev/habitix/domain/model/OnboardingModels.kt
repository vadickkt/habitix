package com.vadymdev.habitix.domain.model

data class InterestCategory(
    val key: String,
    val title: String,
    val emoji: String,
    val cardColor: Long
)

data class HabitTemplate(
    val key: String,
    val title: String,
    val emoji: String
)

data class OnboardingState(
    val completed: Boolean,
    val selectedInterests: Set<String>,
    val selectedHabits: Set<String>
)
