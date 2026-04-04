package com.vadymdev.habitix.presentation.habit.create

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.presentation.habit.habitColor
import com.vadymdev.habitix.presentation.habit.habitIconVector
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.DayOfWeek

@Composable
fun CreateHabitScreen(
    state: CreateHabitUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onTitle: (String) -> Unit,
    onIcon: (String) -> Unit,
    onColor: (String) -> Unit,
    onFrequency: (HabitFrequencyType) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onToggleReminder: () -> Unit,
    vibrationEnabled: Boolean,
    onSave: () -> Unit
) {
    val isUk = language == AppLanguage.UK
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val iconKeys = listOf("water", "book", "fitness", "moon", "mind", "heart", "fork", "music", "pen", "sun", "cup", "steps")
    val colorKeys = listOf("mint", "orange", "purple", "blue", "pink")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFF1F1F1), CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    if (state.editingHabitId == null) stringResource(R.string.create_habit_title_new) else stringResource(R.string.create_habit_title_edit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .background(habitColor(state.selectedColorKey), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habitIconVector(state.selectedIconKey),
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        item {
            Text(stringResource(R.string.create_habit_label_title), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitle,
                placeholder = { Text(stringResource(R.string.create_habit_placeholder_title)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                isError = state.titleError != null,
                singleLine = true
            )
            if (state.titleError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.titleError, color = Color(0xFFD44747), style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Text(stringResource(R.string.create_habit_label_icon), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
                modifier = Modifier.height(164.dp)
            ) {
                items(iconKeys) { key ->
                    val active = state.selectedIconKey == key
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, if (active) primary else Color(0xFFD7D7D7), RoundedCornerShape(18.dp))
                            .background(if (active) primary.copy(alpha = 0.14f) else Color.White, RoundedCornerShape(18.dp))
                            .clickable { onIcon(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = habitIconVector(key),
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.create_habit_label_color), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorKeys.forEach { key ->
                    val active = state.selectedColorKey == key
                    val border by animateColorAsState(if (active) primary else Color.Transparent, label = "color_pick")
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(2.dp, border, CircleShape)
                            .background(habitColor(key), CircleShape)
                            .clickable { onColor(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (active) Text("✓", color = Color.White)
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.create_habit_label_frequency), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FrequencyChip(stringResource(R.string.create_habit_frequency_daily), state.frequency == HabitFrequencyType.DAILY) { onFrequency(HabitFrequencyType.DAILY) }
                FrequencyChip(stringResource(R.string.create_habit_frequency_weekdays), state.frequency == HabitFrequencyType.WEEKDAYS) { onFrequency(HabitFrequencyType.WEEKDAYS) }
                FrequencyChip(stringResource(R.string.create_habit_frequency_custom), state.frequency == HabitFrequencyType.CUSTOM) { onFrequency(HabitFrequencyType.CUSTOM) }
            }

            Column(modifier = Modifier.animateContentSize()) {
                if (state.frequency == HabitFrequencyType.CUSTOM) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val days = listOf(
                        DayOfWeek.MONDAY to if (isUk) stringResource(R.string.weekday_short_mon_uk) else stringResource(R.string.weekday_short_mon_en),
                        DayOfWeek.TUESDAY to if (isUk) stringResource(R.string.weekday_short_tue_uk) else stringResource(R.string.weekday_short_tue_en),
                        DayOfWeek.WEDNESDAY to if (isUk) stringResource(R.string.weekday_short_wed_uk) else stringResource(R.string.weekday_short_wed_en),
                        DayOfWeek.THURSDAY to if (isUk) stringResource(R.string.weekday_short_thu_uk) else stringResource(R.string.weekday_short_thu_en),
                        DayOfWeek.FRIDAY to if (isUk) stringResource(R.string.weekday_short_fri_uk) else stringResource(R.string.weekday_short_fri_en),
                        DayOfWeek.SATURDAY to if (isUk) stringResource(R.string.weekday_short_sat_uk) else stringResource(R.string.weekday_short_sat_en),
                        DayOfWeek.SUNDAY to if (isUk) stringResource(R.string.weekday_short_sun_uk) else stringResource(R.string.weekday_short_sun_en)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        days.forEach { (day, label) ->
                            val active = state.customDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(if (active) primary else Color.White, CircleShape)
                                    .border(1.dp, if (active) primary else Color(0xFFD7D7D7), CircleShape)
                                    .clickable { onToggleDay(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (active) Color.White else TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (state.daysError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(state.daysError, color = Color(0xFFD44747), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(primary.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◔", color = primary)
                }
                Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.create_habit_label_reminder), fontWeight = FontWeight.SemiBold)
                        Text(if (state.reminderEnabled) stringResource(R.string.create_habit_enabled) else stringResource(R.string.create_habit_disabled), color = TextSecondary)
                }
                Switch(checked = state.reminderEnabled, onCheckedChange = { onToggleReminder() })
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (vibrationEnabled) {
                        vibrateForCreate(context)
                    }
                    onSave()
                },
                enabled = !state.isSaving && state.title.trim().length >= 2 && (state.frequency != HabitFrequencyType.CUSTOM || state.customDays.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White,
                    disabledContainerColor = primary.copy(alpha = 0.45f)
                )
            ) {
                val saveLabel = if (state.editingHabitId == null) stringResource(R.string.create_habit_action_create) else stringResource(R.string.create_habit_action_save_changes)
                Text(if (state.isSaving) stringResource(R.string.create_habit_action_saving) else saveLabel, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun FrequencyChip(text: String, active: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .background(if (active) primary.copy(alpha = 0.14f) else Color.White, RoundedCornerShape(22.dp))
            .border(1.dp, if (active) primary else Color(0xFFD9D9D9), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (active) TextPrimary else TextSecondary, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}

private fun vibrateForCreate(context: Context) {
    runCatching {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40L)
        }
    }
}
