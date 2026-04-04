package com.vadymdev.habitix.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.vadymdev.habitix.di.AppContainer

class CloudSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        val container = AppContainer(applicationContext)

        return runCatching {
            container.syncSettingsUseCase(uid)
            container.syncProfileUseCase(uid)
            container.syncUserHabitsUseCase(uid)
            container.syncAchievementsUseCase(uid)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
