package com.vadymdev.habitix.presentation.auth

enum class AuthFailureKind {
    CANCELED_OR_NO_ACCOUNT,
    PROVIDER_UNAVAILABLE,
    TRANSIENT,
    UNKNOWN
}

internal fun classifyAuthFailure(message: String?): AuthFailureKind {
    val raw = message?.lowercase().orEmpty()
    return when {
        raw.contains("canceled") || raw.contains("cancelled") || raw.contains("no account") || raw.contains("account") && raw.contains("missing") -> {
            AuthFailureKind.CANCELED_OR_NO_ACCOUNT
        }
        raw.contains("provider") || raw.contains("credential provider") || raw.contains("play services") || raw.contains("not available") -> {
            AuthFailureKind.PROVIDER_UNAVAILABLE
        }
        raw.contains("network") || raw.contains("timeout") || raw.contains("tempor") || raw.contains("unavailable") -> {
            AuthFailureKind.TRANSIENT
        }
        else -> AuthFailureKind.UNKNOWN
    }
}

internal fun shouldPromoteGuest(kind: AuthFailureKind): Boolean {
    return kind == AuthFailureKind.CANCELED_OR_NO_ACCOUNT || kind == AuthFailureKind.PROVIDER_UNAVAILABLE
}
