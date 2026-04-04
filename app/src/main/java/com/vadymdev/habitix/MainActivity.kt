package com.vadymdev.habitix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.tracing.trace
import com.vadymdev.habitix.notifications.ReminderScheduler
import com.vadymdev.habitix.presentation.HabitixApp
import com.vadymdev.habitix.presentation.startup.StartupViewModel
import com.vadymdev.habitix.sync.CloudSyncScheduler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trace("MainActivity.startup") {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    startupViewModel.startupState.collect { state ->
                        if (state.pushEnabled) {
                            ReminderScheduler.schedule(
                                context = this@MainActivity,
                                hour = state.reminderHour,
                                minute = state.reminderMinute
                            )
                        } else {
                            ReminderScheduler.cancel(this@MainActivity)
                        }

                        updateCloudSync(
                            autoSyncEnabled = state.autoSyncEnabled,
                            hasAuthorizedUser = state.hasAuthorizedUser
                        )
                    }
                }
            }

            enableEdgeToEdge()
            setContent {
                HabitixApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updateCloudSync(autoSyncEnabled: Boolean, hasAuthorizedUser: Boolean) {
        if (hasAuthorizedUser && autoSyncEnabled) {
            CloudSyncScheduler.schedule(this)
        } else {
            CloudSyncScheduler.cancel(this)
        }
    }
}