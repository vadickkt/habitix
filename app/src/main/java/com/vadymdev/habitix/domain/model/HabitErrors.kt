package com.vadymdev.habitix.domain.model

sealed interface HabitDomainError {
	data object DuplicateActiveHabit : HabitDomainError
}

open class HabitDomainException(
	val domainError: HabitDomainError,
	message: String
) : RuntimeException(message)

class DuplicateActiveHabitException : HabitDomainException(
	domainError = HabitDomainError.DuplicateActiveHabit,
	message = "Duplicate active habit for selected date"
)
