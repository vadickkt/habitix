package com.vadymdev.habitix.domain.model

data class UserSession(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)
