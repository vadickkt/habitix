package com.vadymdev.habitix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vadymdev.habitix.presentation.HabitixApp
import com.vadymdev.habitix.ui.theme.HabitixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitixTheme {
                HabitixApp()
            }
        }
    }
}