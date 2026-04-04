package com.vadymdev.habitix.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.presentation.habit.habitColor
import com.vadymdev.habitix.presentation.habit.habitIcon
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.format.TextStyle
import kotlin.math.abs
import java.util.Locale

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    language: AppLanguage,
    onDateSelected: (LocalDate) -> Unit,
    onToggleHabit: (Habit) -> Unit,
    vibrationEnabled: Boolean,
    onConsumeAchievementEvent: (Long) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onCreateHabit: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val anchorWeekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val initialPage = 10_000
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 20_000 })
    val isUk = language == AppLanguage.UK
    val locale = remember(language) { if (isUk) Locale.forLanguageTag("uk") else Locale.ENGLISH }

    LaunchedEffect(pagerState.currentPage) {
        val weekOffset = pagerState.currentPage - initialPage
        val weekStart = anchorWeekStart.plusDays(weekOffset * 7L)
        val expectedDate = weekStart.plusDays((state.selectedDate.dayOfWeek.value - 1).toLong())
        if (expectedDate != state.selectedDate) {
            onDateSelected(expectedDate)
        }
    }

    LaunchedEffect(state.achievementEvent?.id) {
        val event = state.achievementEvent ?: return@LaunchedEffect
        if (vibrationEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        snackbarHostState.showSnackbar(
            if (isUk) {
                "Досягнення відкрито: ${event.title} (+${event.xpReward} XP)"
            } else {
                "Achievement unlocked: ${event.title} (+${event.xpReward} XP)"
            }
        )
        onConsumeAchievementEvent(event.id)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding(),
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomBar(
                onHome = {},
                onStats = onOpenStats,
                onProfile = onOpenProfile,
                onSettings = onOpenSettings,
                isUk = isUk,
                activeTab = "home"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                HeaderBlock(state = state, isUk = isUk)
            }

            item {
                CalendarStrip(
                    pagerState = pagerState,
                    anchorWeekStart = anchorWeekStart,
                    selected = state.selectedDate,
                    locale = locale,
                    onSelect = onDateSelected
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isUk) "Звички на сьогодні" else "Habits for today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(280)) + expandVertically(animationSpec = tween(280))
                ) {
                    SwipeableHabitRow(
                        habit = habit,
                        isUk = isUk,
                        onToggle = {
                            if (vibrationEnabled && !habit.isCompletedForSelectedDate) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onToggleHabit(habit)
                        },
                        onDelete = {
                            if (vibrationEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onDeleteHabit(habit)
                        },
                        onEdit = { onEditHabit(habit) }
                    )
                }
            }

            item {
                AddHabitButton(onCreateHabit = onCreateHabit, isUk = isUk)
            }
        }
    }
}

