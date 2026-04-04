package com.vadymdev.habitix.domain.model

enum class SyncFailureKind {
    TRANSIENT,
    PERMANENT
}

enum class SyncTarget {
    ORCHESTRATOR,
    SETTINGS,
    PROFILE,
    HABITS,
    ACHIEVEMENTS
}

class SyncDomainException(
    val kind: SyncFailureKind,
    val target: SyncTarget,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
