package com.vadymdev.habitix

import android.app.Application
import androidx.tracing.trace
import androidx.work.Configuration
import androidx.work.WorkManager
import com.vadymdev.habitix.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.android.get
import org.koin.core.context.startKoin

class HabitixApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		trace("HabitixApplication.startup") {
			startKoin {
				androidContext(this@HabitixApplication)
				modules(appModule)
			}

			WorkManager.initialize(
				this,
				Configuration.Builder()
					.setWorkerFactory(get())
					.build()
			)
		}
	}
}
