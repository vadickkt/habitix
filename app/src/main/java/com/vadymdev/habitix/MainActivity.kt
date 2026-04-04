package com.vadymdev.habitix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.vadymdev.habitix.data.local.SettingsPreferencesDataSource
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.notifications.ReminderScheduler
import com.vadymdev.habitix.presentation.HabitixApp
import com.vadymdev.habitix.sync.CloudSyncScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsDataSource = SettingsPreferencesDataSource(this)

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (isDestroyed) return@AuthStateListener
            lifecycleScope.launch {
                val settings = settingsDataSource.getCurrentSettings()
                updateCloudSync(settings = settings, hasAuthorizedUser = auth.currentUser != null)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)

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

                        updateCloudSync(
                            settings = settings,
                            hasAuthorizedUser = firebaseAuth.currentUser != null
                        )
                    }
            }
        }

        enableEdgeToEdge()
        setContent {
            HabitixApp()
        }
    }

    override fun onDestroy() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }

    private fun updateCloudSync(settings: AppSettings, hasAuthorizedUser: Boolean) {
        if (hasAuthorizedUser && settings.autoSyncEnabled) {
            CloudSyncScheduler.schedule(this)
        } else {
            CloudSyncScheduler.cancel(this)
        }
    }
}