package com.vadymdev.habitix.presentation.auth

enum class AuthFailureKind {
    USER_CANCELED,
    NO_ACCOUNT,
    PROVIDER_UNAVAILABLE,
    TRANSIENT,
    UNKNOWN
}

internal fun classifyAuthFailure(error: Throwable?): AuthFailureKind {
    val raw = error?.message?.lowercase().orEmpty()
    val type = error?.javaClass?.simpleName?.lowercase().orEmpty()

    return when {
        raw.contains("canceled") || raw.contains("cancelled") || raw.contains("dismiss") || type.contains("cancel") -> AuthFailureKind.USER_CANCELED
        raw.contains("no credentials available") || raw.contains("no account") || raw.contains("account") && raw.contains("missing") -> AuthFailureKind.NO_ACCOUNT
        raw.contains("provider") || raw.contains("credential provider") || raw.contains("play services") || raw.contains("not available") -> {
            AuthFailureKind.PROVIDER_UNAVAILABLE
        }
        raw.contains("network") || raw.contains("timeout") || raw.contains("tempor") -> {
            AuthFailureKind.TRANSIENT
        }
        else -> AuthFailureKind.UNKNOWN
    }
}

internal fun classifyAuthFailure(message: String?): AuthFailureKind {
    return classifyAuthFailure(message?.let { RuntimeException(it) })
}

internal fun shouldPromoteGuest(kind: AuthFailureKind): Boolean {
    return kind == AuthFailureKind.NO_ACCOUNT || kind == AuthFailureKind.PROVIDER_UNAVAILABLE
}
