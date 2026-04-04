package com.vadymdev.habitix.data.repository.sync

import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncErrorMapperTest {

    @Test
    fun existingSyncDomainException_isReturnedAsIs() {
        val original = SyncDomainException(
            kind = SyncFailureKind.PERMANENT,
            target = SyncTarget.SETTINGS,
            message = "already-typed"
        )

        val result = mapSyncThrowable(SyncTarget.HABITS, original)

        assertSame(original, result)
    }

    @Test
    fun firebaseWrapperWithoutFirestoreInstance_fallsBackToUnknownTransient() {
        val wrapped = IllegalStateException("outer", RuntimeException("middle", RuntimeException("inner")))

        val result = mapSyncThrowable(SyncTarget.PROFILE, wrapped)

        assertEquals(SyncFailureKind.TRANSIENT, result.kind)
        assertEquals(SyncTarget.PROFILE, result.target)
        assertTrue(result.message?.contains("Unknown sync failure") == true)
        assertEquals(wrapped, result.cause)
    }

    @Test
    fun unknownThrowable_mapsToTransientUnknownFailure() {
        val error = RuntimeException("network")

        val result = mapSyncThrowable(SyncTarget.ORCHESTRATOR, error)

        assertEquals(SyncFailureKind.TRANSIENT, result.kind)
        assertEquals(SyncTarget.ORCHESTRATOR, result.target)
        assertTrue(result.message?.contains("Unknown sync failure") == true)
        assertEquals(error, result.cause)
    }

    @Test
    fun nestedUnknownThrowable_mapsToTransientAndPreservesRootCauseWrapper() {
        val error = IllegalArgumentException("top", IllegalStateException("inner"))

        val result = mapSyncThrowable(SyncTarget.SETTINGS, error)

        assertEquals(SyncFailureKind.TRANSIENT, result.kind)
        assertEquals(SyncTarget.SETTINGS, result.target)
        assertEquals(error, result.cause)
    }
}
