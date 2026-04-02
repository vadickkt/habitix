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
import com.vadymdev.habitix.R
import com.vadymdev.habitix.data.local.room.HabitixDatabase
import com.vadymdev.habitix.data.repository.HabitRepositoryImpl
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
            hiddenDayDao = database.hiddenHabitDayDao()
        )

        val incomplete = repository.getIncompleteHabitsForDate(LocalDate.now())
        if (incomplete.isEmpty()) return Result.success()

        createChannelIfNeeded(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Є невиконані звички")
            .setContentText("Сьогодні ще ${incomplete.size} звички чекають на тебе")
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
