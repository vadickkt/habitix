package com.vadymdev.habitix.presentation.habit.edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.HabitFrequencyType
import com.vadymdev.habitix.presentation.habit.create.CreateHabitUiState
import com.vadymdev.habitix.presentation.habit.habitColor
import com.vadymdev.habitix.presentation.habit.habitIconVector
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.DayOfWeek

@Composable
fun EditHabitScreen(
    state: CreateHabitUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onTitle: (String) -> Unit,
    onIcon: (String) -> Unit,
    onColor: (String) -> Unit,
    onFrequency: (HabitFrequencyType) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onToggleReminder: () -> Unit,
    onSave: () -> Unit
) {
    val isUk = language == AppLanguage.UK
    val iconKeys = listOf("water", "book", "fitness", "moon", "mind", "heart", "fork", "music", "pen", "sun", "cup", "steps")
    val colorKeys = listOf("mint", "orange", "purple", "blue", "pink")
    var heroEntered by remember(state.editingHabitId) { mutableStateOf(false) }

    LaunchedEffect(state.editingHabitId) {
        heroEntered = true
    }

    val previewCardColor by animateColorAsState(
        targetValue = habitColor(state.selectedColorKey),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 260f),
        label = "edit_preview_color"
    )
    val heroScale by animateFloatAsState(
        targetValue = if (heroEntered) 1f else 0.72f,
        animationSpec = spring(dampingRatio = 0.73f, stiffness = 300f),
        label = "edit_hero_scale"
    )
    val heroAlpha by animateFloatAsState(
        targetValue = if (heroEntered) 1f else 0.35f,
        animationSpec = tween(260),
        label = "edit_hero_alpha"
    )

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
                Text(stringResource(R.string.create_habit_title_edit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        item {
            EditHabitPreviewCard(
                state = state,
                previewCardColor = previewCardColor,
                heroScale = heroScale,
                heroAlpha = heroAlpha
            )
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
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(94.dp)
            ) {
                items(iconKeys) { key ->
                    val active = state.selectedIconKey == key
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, if (active) BrandGreen else Color(0xFFD7D7D7), RoundedCornerShape(18.dp))
                            .background(if (active) Color(0xFFE8F8EF) else Color.White, RoundedCornerShape(18.dp))
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(2.dp, if (active) BrandGreen else Color.Transparent, CircleShape)
                            .background(habitColor(key), CircleShape)
                            .clickable { onColor(key) }
                    )
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
                                .background(if (active) BrandGreen else Color.White, CircleShape)
                                .border(1.dp, if (active) BrandGreen else Color(0xFFD7D7D7), CircleShape)
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
                        .background(Color(0xFFE8F8EF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◔", color = BrandGreen)
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
                onClick = onSave,
                enabled = !state.isSaving && state.title.trim().length >= 2 && (state.frequency != HabitFrequencyType.CUSTOM || state.customDays.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = BrandGreen.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    if (state.isSaving) stringResource(R.string.create_habit_action_saving) else stringResource(R.string.create_habit_action_save_changes),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun FrequencyChip(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (active) Color(0xFFE8F8EF) else Color.White, RoundedCornerShape(22.dp))
            .border(1.dp, if (active) BrandGreen else Color(0xFFD9D9D9), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (active) TextPrimary else TextSecondary, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}
