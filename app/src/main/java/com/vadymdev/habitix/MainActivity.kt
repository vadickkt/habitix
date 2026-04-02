package com.vadymdev.habitix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.notifications.ReminderScheduler
import com.vadymdev.habitix.presentation.HabitixApp
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SettingsPreferencesDataSource(this@MainActivity)
                    .observeSettings()
                    .collect { settings ->
                        if (settings.pushEnabled) {
                            ReminderScheduler.schedule(this@MainActivity)
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