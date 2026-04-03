package com.vadymdev.habitix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.notifications.ReminderScheduler
import com.vadymdev.habitix.presentation.HabitixApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsDataSource = SettingsPreferencesDataSource(this)

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