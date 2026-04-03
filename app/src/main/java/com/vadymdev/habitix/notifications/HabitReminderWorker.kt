package com.vadymdev.habitix.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.R
import com.vadymdev.habitix.data.local.room.HabitixDatabase
import com.vadymdev.habitix.data.repository.HabitRepositoryImpl
import com.vadymdev.habitix.domain.model.AppLanguage
import java.time.LocalDate

class HabitReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = HabitixDatabase.get(applicationContext)
        val repository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            completionDao = database.habitCompletionDao(),
            hiddenDayDao = database.hiddenHabitDayDao(),
            achievementUnlockDao = database.achievementUnlockDao()
        )

        val settings = SettingsPreferencesDataSource(applicationContext).getCurrentSettings()
        if (!settings.pushEnabled) return Result.success()

        val incomplete = repository.getIncompleteHabitsForDate(LocalDate.now())
            .filter { it.reminderEnabled }
        if (incomplete.isEmpty()) return Result.success()

        createChannelIfNeeded(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val isUk = settings.language == AppLanguage.UK
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (isUk) "Є невиконані звички" else "You still have unfinished habits")
            .setContentText(
                if (isUk) {
                    "Сьогодні ще ${incomplete.size} звички чекають на тебе"
                } else {
                    "You still have ${incomplete.size} habits to complete today"
                }
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1089, notification)
        return Result.success()
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Нагадування про невиконані звички"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "habitix_reminders"
    }
}
