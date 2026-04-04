package com.vadymdev.habitix.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SyncOrchestratorExecutionTest {

    @Test
    fun fullScope_mergesConcurrentCalls_withSingleFlight() = runBlocking {
        val inFlight = AtomicInteger(0)
        val maxInFlight = AtomicInteger(0)
        val orchestrator = buildSyncOrchestrator(
            onSettingsSync = {
                val running = inFlight.incrementAndGet()
                while (true) {
                    val currentMax = maxInFlight.get()
                    if (running <= currentMax || maxInFlight.compareAndSet(currentMax, running)) break
                }
                delay(80)
                inFlight.decrementAndGet()
            }
        )

        withContext(Dispatchers.Default) {
            awaitAll(
                async { orchestrator("uid-1", SyncScope.FULL) },
                async { orchestrator("uid-1", SyncScope.FULL) }
            )
        }

        assertEquals(1, maxInFlight.get())
    }

    @Test
    fun offline_shortCircuitsSync_andRequestsDeferredSync() = runBlocking {
        val recorder = CallRecorder()
        val deferredCalls = AtomicInteger(0)

        val orchestrator = buildSyncOrchestrator(
            recorder = recorder,
            networkAvailable = { false },
            onDeferredSyncRequested = { deferredCalls.incrementAndGet() }
        )

        val result = orchestrator("uid-1", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertTrue(recorder.calls.isEmpty())
        assertEquals(1, deferredCalls.get())
    }
}
