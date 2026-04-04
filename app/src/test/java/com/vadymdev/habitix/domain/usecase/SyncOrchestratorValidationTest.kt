package com.vadymdev.habitix.domain.usecase

import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncOrchestratorValidationTest {

    @Test
    fun blankUserId_returnsTypedError() = runBlocking {
        val orchestrator = buildSyncOrchestrator()

        val result = orchestrator("", SyncScope.FULL)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SyncDomainException)
        assertEquals(SyncFailureKind.PERMANENT, (error as SyncDomainException).kind)
        assertEquals(SyncTarget.ORCHESTRATOR, error.target)
    }

    @Test
    fun blankUserId_doesNotInvokeAnySyncStage() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildSyncOrchestrator(recorder = recorder)

        val result = orchestrator("", SyncScope.FULL)

        assertTrue(result.isFailure)
        assertTrue(recorder.calls.isEmpty())
    }
}
