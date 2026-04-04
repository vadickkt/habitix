package com.vadymdev.habitix.data.repository.sync

import com.google.firebase.firestore.FirebaseFirestoreException
import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget

fun mapSyncThrowable(target: SyncTarget, throwable: Throwable): SyncDomainException {
    if (throwable is SyncDomainException) {
        return throwable
    }

    val firestoreError = generateSequence(throwable) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()

    if (firestoreError != null) {
        return SyncDomainException(
            kind = firestoreCodeToKind(firestoreError),
            target = target,
            message = "Firestore sync failure for $target: ${firestoreError.code}",
            cause = throwable
        )
    }

    return SyncDomainException(
        kind = SyncFailureKind.TRANSIENT,
        target = target,
        message = "Unknown sync failure for $target",
        cause = throwable
    )
}

private fun firestoreCodeToKind(error: FirebaseFirestoreException): SyncFailureKind {
    return when (error.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED,
        FirebaseFirestoreException.Code.INVALID_ARGUMENT,
        FirebaseFirestoreException.Code.FAILED_PRECONDITION,
        FirebaseFirestoreException.Code.UNAUTHENTICATED,
        FirebaseFirestoreException.Code.NOT_FOUND,
        FirebaseFirestoreException.Code.ALREADY_EXISTS -> SyncFailureKind.PERMANENT
        else -> SyncFailureKind.TRANSIENT
    }
}
