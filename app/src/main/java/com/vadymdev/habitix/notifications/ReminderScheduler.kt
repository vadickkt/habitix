package com.vadymdev.habitix.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val periodic = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "habitix_periodic_reminders",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )

        val immediate = OneTimeWorkRequestBuilder<HabitReminderWorker>().build()
        workManager.enqueueUniqueWork(
            "habitix_immediate_reminder_check",
            ExistingWorkPolicy.REPLACE,
            immediate
        )
    }
}
