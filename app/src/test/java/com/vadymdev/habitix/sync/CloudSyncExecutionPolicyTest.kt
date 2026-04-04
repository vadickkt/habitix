package com.vadymdev.habitix.sync

import androidx.work.ListenableWorker
import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
import com.vadymdev.habitix.domain.usecase.SyncScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class CloudSyncExecutionPolicyTest {

    @Test
    fun maxRunAttemptsReached_returnsSuccessWithoutSyncCall() = runBlocking {
        val calls = AtomicInteger(0)
        val policy = buildPolicy { calls.incrementAndGet() }

        val result = policy.execute(runAttemptCount = 5, userId = "uid", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(0, calls.get())
    }

    @Test
    fun blankUserId_returnsSuccessWithoutSyncCall() = runBlocking {
        val calls = AtomicInteger(0)
        val policy = buildPolicy { calls.incrementAndGet() }

        val result = policy.execute(runAttemptCount = 0, userId = " ", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(0, calls.get())
    }

    @Test
    fun nullUserId_returnsSuccessWithoutSyncCall() = runBlocking {
        val calls = AtomicInteger(0)
        val policy = buildPolicy { calls.incrementAndGet() }

        val result = policy.execute(runAttemptCount = 0, userId = null, log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(0, calls.get())
    }

    @Test
    fun syncSuccess_returnsSuccess() = runBlocking {
        val calls = AtomicInteger(0)
        val policy = buildPolicy { calls.incrementAndGet() }

        val result = policy.execute(runAttemptCount = 1, userId = "uid", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(1, calls.get())
    }

    @Test
    fun transientSyncDomainError_returnsRetry() = runBlocking {
        val policy = buildPolicy {
            throw SyncDomainException(
                kind = SyncFailureKind.TRANSIENT,
                target = SyncTarget.ORCHESTRATOR,
                message = "temporary"
            )
        }

        val result = policy.execute(runAttemptCount = 1, userId = "uid", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun permanentSyncDomainError_returnsSuccessNoRetry() = runBlocking {
        val policy = buildPolicy {
            throw SyncDomainException(
                kind = SyncFailureKind.PERMANENT,
                target = SyncTarget.SETTINGS,
                message = "invalid"
            )
        }

        val result = policy.execute(runAttemptCount = 1, userId = "uid", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun wrappedTransientSyncDomainError_returnsRetry() = runBlocking {
        val policy = buildPolicy {
            throw IllegalStateException(
                "wrapper",
                SyncDomainException(
                    kind = SyncFailureKind.TRANSIENT,
                    target = SyncTarget.HABITS,
                    message = "wrapped"
                )
            )
        }

        val result = policy.execute(runAttemptCount = 1, userId = "uid", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun unknownError_returnsRetry() = runBlocking {
        val policy = buildPolicy { throw RuntimeException("network") }

        val result = policy.execute(runAttemptCount = 1, userId = "uid", log = { _, _, _ -> })

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    private fun buildPolicy(
        syncAction: suspend (String) -> Unit
    ): CloudSyncExecutionPolicy {
        return CloudSyncExecutionPolicy(
            maxRetryAttempts = 5,
            syncScope = SyncScope.FULL,
            syncAction = syncAction
        )
    }
}
