package com.vadymdev.habitix

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.notifications.ReminderScheduler
import com.vadymdev.habitix.presentation.HabitixApp
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle

class MainActivity : ComponentActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 3301
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsDataSource = SettingsPreferencesDataSource(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lifecycleScope.launch {
                val granted = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                val askedOnce = settingsDataSource.hasAskedNotificationPermission()
                if (!granted && !askedOnce) {
                    settingsDataSource.markNotificationPermissionAsked()
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsDataSource
                    .observeSettings()
                    .collect { settings ->
                        if (settings.pushEnabled) {
                            ReminderScheduler.schedule(
                                context = this@MainActivity,
                                hour = settings.reminderHour,
                                minute = settings.reminderMinute
                            )
                        } else {
                            ReminderScheduler.cancel(this@MainActivity)
                        }
                    }
            }
        }

        enableEdgeToEdge()
        setContent {
            HabitixApp()
        }
    }
}