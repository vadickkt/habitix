package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SyncOrchestratorRetryPolicyTest {

    @Test
    fun fullScope_retriesTransientFailure_andEventuallySucceeds() = runBlocking {
        val recorder = CallRecorder()
        val settingsAttempts = AtomicInteger(0)

        val orchestrator = buildSyncOrchestrator(
            recorder = recorder,
            onSettingsSync = {
                if (settingsAttempts.getAndIncrement() == 0) {
                    throw RuntimeException("Transient")
                }
            }
        )

        val result = orchestrator("uid-1", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertEquals(2, settingsAttempts.get())
        assertTrue(recorder.calls.count { it == "settings" } >= 2)
    }

    @Test
    fun noRetryForPermanentSyncError() = runBlocking {
        val typedError = SyncDomainException(
            kind = SyncFailureKind.PERMANENT,
            target = SyncTarget.SETTINGS,
            message = "non-retryable"
        )
        val attempts = AtomicInteger(0)

        val orchestrator = buildSyncOrchestrator(
            maxRetries = 3,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw typedError
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertFalse(result.isSuccess)
        assertEquals(1, attempts.get())
        assertTrue(result.exceptionOrNull() is SyncDomainException)
    }

    @Test
    fun noRetryForIllegalArgumentException() = runBlocking {
        val attempts = AtomicInteger(0)
        val orchestrator = buildSyncOrchestrator(
            maxRetries = 4,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw IllegalArgumentException("bad-request")
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isFailure)
        assertEquals(1, attempts.get())
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun transientSyncError_retriesUpToMaxAttempts_thenFails() = runBlocking {
        val attempts = AtomicInteger(0)
        val transientError = SyncDomainException(
            kind = SyncFailureKind.TRANSIENT,
            target = SyncTarget.SETTINGS,
            message = "temporary"
        )

        val orchestrator = buildSyncOrchestrator(
            maxRetries = 3,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw transientError
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isFailure)
        assertEquals(3, attempts.get())
        assertEquals(transientError, result.exceptionOrNull())
    }

    @Test
    fun maxRetriesLessThanOne_isCoercedToSingleAttempt() = runBlocking {
        val attempts = AtomicInteger(0)
        val orchestrator = buildSyncOrchestrator(
            maxRetries = 0,
            onSettingsSync = {
                attempts.incrementAndGet()
                throw RuntimeException("boom")
            }
        )

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isFailure)
        assertEquals(1, attempts.get())
    }
}
