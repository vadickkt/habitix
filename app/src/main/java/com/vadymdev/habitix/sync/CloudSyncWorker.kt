package com.vadymdev.habitix.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.vadymdev.habitix.di.AppContainer
import com.vadymdev.habitix.domain.model.SyncDomainException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.usecase.SyncScope

class CloudSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "CloudSyncWorker"
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    private val policy = CloudSyncExecutionPolicy(
        maxRetryAttempts = MAX_RETRY_ATTEMPTS,
        syncScope = SyncScope.FULL,
        syncAction = { uid ->
            val container = AppContainer(applicationContext)
            container.syncOrchestratorUseCase(uid, SyncScope.FULL).getOrThrow()
        }
    )

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return policy.execute(runAttemptCount, uid) { level, message, error ->
            when (level) {
                CloudSyncLogLevel.DEBUG -> Log.d(TAG, message)
                CloudSyncLogLevel.WARN -> Log.w(TAG, message, error)
            }
        }
    }
}

internal enum class CloudSyncLogLevel {
    DEBUG,
    WARN
}

internal class CloudSyncExecutionPolicy(
    private val maxRetryAttempts: Int,
    private val syncScope: SyncScope,
    private val syncAction: suspend (userId: String) -> Unit
) {
    suspend fun execute(
        runAttemptCount: Int,
        userId: String?,
        log: (CloudSyncLogLevel, String, Throwable?) -> Unit
    ): ListenableWorker.Result {
        if (runAttemptCount >= maxRetryAttempts) {
            log(CloudSyncLogLevel.WARN, "Max retry attempts reached for current run window", null)
            return ListenableWorker.Result.success()
        }

        if (userId.isNullOrBlank()) return ListenableWorker.Result.success()

        return runCatching {
            syncAction(userId)
        }.fold(
            onSuccess = {
                log(CloudSyncLogLevel.DEBUG, "Periodic sync completed successfully for $syncScope", null)
                ListenableWorker.Result.success()
            },
            onFailure = { error ->
                log(CloudSyncLogLevel.WARN, "Periodic sync failed on attempt ${runAttemptCount + 1}", error)
                if (shouldRetry(error)) ListenableWorker.Result.retry() else ListenableWorker.Result.success()
            }
        )
    }

    private fun shouldRetry(error: Throwable): Boolean {
        val syncError = generateSequence(error) { it.cause }
            .filterIsInstance<SyncDomainException>()
            .firstOrNull()

        if (syncError != null) {
            return syncError.kind == SyncFailureKind.TRANSIENT
        }

        return true
    }
}
