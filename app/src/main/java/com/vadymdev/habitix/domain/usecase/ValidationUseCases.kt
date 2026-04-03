package com.vadymdev.habitix.domain.usecase

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

class ValidateHabitTitleUseCase {
    operator fun invoke(value: String): ValidationResult {
        val trimmed = value.trim()
        return when {
            trimmed.isBlank() -> ValidationResult(false, "Вкажіть ціль або назву звички")
            trimmed.length < 2 -> ValidationResult(false, "Назва має містити щонайменше 2 символи")
            else -> ValidationResult(true)
        }
    }
}

class ValidateProfileNameUseCase {
    operator fun invoke(value: String): ValidationResult {
        val trimmed = value.trim()
        return when {
            trimmed.isBlank() -> ValidationResult(false, "Ім'я не може бути порожнім")
            trimmed.length < 2 -> ValidationResult(false, "Ім'я має містити щонайменше 2 символи")
            else -> ValidationResult(true)
        }
    }
}
