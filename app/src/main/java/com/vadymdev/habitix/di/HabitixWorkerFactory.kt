package com.vadymdev.habitix.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.vadymdev.habitix.domain.usecase.GetCurrentSettingsUseCase
import com.vadymdev.habitix.domain.usecase.GetIncompleteHabitsForDateUseCase
import com.vadymdev.habitix.domain.usecase.SyncOrchestratorUseCase
import com.vadymdev.habitix.notifications.HabitReminderWorker
import com.vadymdev.habitix.sync.CloudSyncWorker

class HabitixWorkerFactory(
    private val firebaseAuth: FirebaseAuth,
    private val syncOrchestratorUseCase: SyncOrchestratorUseCase,
    private val getCurrentSettingsUseCase: GetCurrentSettingsUseCase,
    private val getIncompleteHabitsForDateUseCase: GetIncompleteHabitsForDateUseCase
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CloudSyncWorker::class.java.name -> {
                CloudSyncWorker(
                    appContext = appContext,
                    params = workerParameters,
                    firebaseAuth = firebaseAuth,
                    syncOrchestratorUseCase = syncOrchestratorUseCase
                )
            }

            HabitReminderWorker::class.java.name -> {
                HabitReminderWorker(
                    appContext = appContext,
                    params = workerParameters,
                    getCurrentSettingsUseCase = getCurrentSettingsUseCase,
                    getIncompleteHabitsForDateUseCase = getIncompleteHabitsForDateUseCase
                )
            }

            else -> null
        }
    }
}
