package com.vadymdev.habitix.sync

import android.util.Log
import android.content.Context
import androidx.work.CoroutineWorker
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

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Max retry attempts reached for current run window")
            return Result.success()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) return Result.success()

        val container = AppContainer(applicationContext)

        return runCatching {
            container.syncOrchestratorUseCase(uid, SyncScope.FULL).getOrThrow()
        }.fold(
            onSuccess = {
                Log.d(TAG, "Periodic sync completed successfully")
                Result.success()
            },
            onFailure = { error ->
                Log.w(TAG, "Periodic sync failed on attempt ${runAttemptCount + 1}", error)
                if (shouldRetry(error)) Result.retry() else Result.success()
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
