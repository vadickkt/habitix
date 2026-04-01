package com.vadymdev.habitix.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.presentation.habit.habitColor
import com.vadymdev.habitix.presentation.habit.habitIcon
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onDateSelected: (LocalDate) -> Unit,
    onToggleHabit: (Habit) -> Unit,
    onCreateHabit: () -> Unit
) {
    val calendarDays = remember(state.selectedDate) {
        (-3..3).map { state.selectedDate.plusDays(it.toLong()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                HeaderBlock(state = state)
            }

            item {
                CalendarStrip(
                    days = calendarDays,
                    selected = state.selectedDate,
                    onSelect = onDateSelected
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Звички на сьогодні", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onCreateHabit)
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            items(state.habits, key = { it.id }) { habit ->
                HabitRowCard(habit = habit, onToggle = { onToggleHabit(habit) })
            }

            item {
                AddHabitButton(onCreateHabit = onCreateHabit)
            }
        }

        BottomBar()
    }
}

@Composable
private fun HeaderBlock(state: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text("Привіт 👋", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Text("Гарного дня!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFDDF2E8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${state.completedCount}/${if (state.totalCount == 0) 1 else state.totalCount}",
                color = BrandGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarStrip(days: List<LocalDate>, selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
        ) {
            items(days, key = { it.toEpochDay() }) { date ->
                val active = date == selected
                val bg by animateColorAsState(
                    targetValue = if (active) BrandGreen else Color.Transparent,
                    animationSpec = tween(220),
                    label = "calendar_bg"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bg)
                        .clickable { onSelect(date) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("uk")),
                        color = if (active) Color.White else TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (active) Color.White else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitRowCard(habit: Habit, onToggle: () -> Unit) {
    val done = habit.isCompletedForSelectedDate
    val background by animateColorAsState(if (done) Color(0xFFD8EEE6) else Color(0xFFFFFFFF), label = "habit_bg")
    val border by animateColorAsState(if (done) Color(0xFF87D5B5) else Color(0xFFD8D8D8), label = "habit_border")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .background(background, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(habitColor(habit.colorKey), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(habitIcon(habit.iconKey), style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = "🔥 ${habit.streakDays} днів",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Text("…", color = TextSecondary, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.size(10.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, if (done) BrandGreen else Color(0xFFCFCFCF), CircleShape)
                .background(if (done) BrandGreen else Color.Transparent, CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = done,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith (fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "habit_toggle"
            ) { checked ->
                Text(
                    text = if (checked) "✓" else "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddHabitButton(onCreateHabit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD8D8D8), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCreateHabit)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("+   Додати нову звичку", color = TextSecondary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BottomBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFE8E8E8)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomTab(icon = "⌂", label = "Головна", active = true)
            BottomTab(icon = "⌁", label = "Статистика", active = false)
            BottomTab(icon = "◡", label = "Профіль", active = false)
            BottomTab(icon = "⚙", label = "Налаштування", active = false)
        }
    }
}

@Composable
private fun BottomTab(icon: String, label: String, active: Boolean) {
    val pillColor by animateColorAsState(
        targetValue = if (active) Color(0xFFE7F8EF) else Color.Transparent,
        animationSpec = tween(220),
        label = "tab_pill"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (active) 14.dp else 10.dp,
        animationSpec = tween(220),
        label = "tab_padding"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(pillColor)
            .padding(horizontal = horizontalPadding, vertical = 6.dp)
    ) {
        Text(icon, color = if (active) BrandGreen else TextSecondary)
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (active) BrandGreen else TextSecondary,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
