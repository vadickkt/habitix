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
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.presentation.habit.habitColor
import com.vadymdev.habitix.presentation.habit.habitIcon
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.format.TextStyle
import kotlin.math.abs
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onDateSelected: (LocalDate) -> Unit,
    onToggleHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onCreateHabit: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val anchorWeekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val initialPage = 10_000
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 20_000 })

    LaunchedEffect(pagerState.currentPage) {
        val weekOffset = pagerState.currentPage - initialPage
        val weekStart = anchorWeekStart.plusDays(weekOffset * 7L)
        val expectedDate = weekStart.plusDays((state.selectedDate.dayOfWeek.value - 1).toLong())
        if (expectedDate != state.selectedDate) {
            onDateSelected(expectedDate)
        }
    }

    fun requestDeleteWithUndo(habit: Habit) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Звичка буде видалена з сьогодні і майбутнього",
                actionLabel = "Undo",
                withDismissAction = true
            )
            if (result != SnackbarResult.ActionPerformed) {
                onDeleteHabit(habit)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomBar(
                onHome = {},
                onStats = onOpenStats,
                onProfile = onOpenProfile,
                onSettings = onOpenSettings,
                activeTab = "home"
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                HeaderBlock(state = state)
            }

            item {
                CalendarStrip(
                    pagerState = pagerState,
                    anchorWeekStart = anchorWeekStart,
                    selected = state.selectedDate,
                    onSelect = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDateSelected(it)
                    }
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
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(280)) + expandVertically(animationSpec = tween(280))
                ) {
                    SwipeableHabitRow(
                        habit = habit,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleHabit(habit)
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            requestDeleteWithUndo(habit)
                        },
                        onEdit = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEditHabit(habit)
                        }
                    )
                }
            }

            item {
                AddHabitButton(onCreateHabit = onCreateHabit)
            }
        }
    }
}

@Composable
private fun SwipeableHabitRow(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(dismissState = dismissState)
        },
        content = {
            HabitRowCard(
                habit = habit,
                onToggle = onToggle,
                onDelete = onDelete,
                onEdit = onEdit
            )
        }
    )
}

@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val bgColor by animateColorAsState(
        targetValue = when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFFDDF3E8)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFFFE3E3)
            else -> Color.Transparent
        },
        label = "swipe_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color(0xFF1F8A5B))
        Text("Видалити", color = Color(0xFFD44747), fontWeight = FontWeight.SemiBold)
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
private fun CalendarStrip(
    pagerState: androidx.compose.foundation.pager.PagerState,
    anchorWeekStart: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp)
    ) {
        HorizontalPager(state = pagerState) { page ->
            val weekOffset = page - 10_000
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val weekStart = anchorWeekStart.plusDays(weekOffset * 7L)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = pageOffset * 28f
                        alpha = 1f - abs(pageOffset) * 0.12f
                    }
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..6).forEach { index ->
                    val date = weekStart.plusDays(index.toLong())
                    val active = date == selected
                    val bg by animateColorAsState(
                        targetValue = if (active) BrandGreen else Color.Transparent,
                        animationSpec = tween(220),
                        label = "calendar_bg"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (active) 1.06f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 350f),
                        label = "calendar_scale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .clickable { onSelect(date) }
                            .padding(vertical = 8.dp),
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
}

@Composable
private fun HabitRowCard(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val done = habit.isCompletedForSelectedDate
    val background by animateColorAsState(if (done) Color(0xFFD8EEE6) else Color(0xFFFFFFFF), label = "habit_bg")
    val border by animateColorAsState(if (done) Color(0xFF87D5B5) else Color(0xFFD8D8D8), label = "habit_border")
    var menuOpened by remember { mutableStateOf(false) }
    val toggleScale by animateFloatAsState(
        targetValue = if (done) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "toggle_scale"
    )

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

        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Дії",
                tint = TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { menuOpened = true }
            )
            DropdownMenu(expanded = menuOpened, onDismissRequest = { menuOpened = false }) {
                DropdownMenuItem(
                    text = { Text("Редагувати") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuOpened = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Видалити") },
                    onClick = {
                        menuOpened = false
                        onDelete()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))

        Box(
            modifier = Modifier
                .scale(toggleScale)
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
private fun BottomBar(
    onHome: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    activeTab: String
) {
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
            BottomTab(icon = Icons.Rounded.Home, label = "Головна", active = activeTab == "home", onClick = onHome)
            BottomTab(icon = Icons.Rounded.Analytics, label = "Статистика", active = activeTab == "stats", onClick = onStats)
            BottomTab(icon = Icons.Rounded.Person, label = "Профіль", active = activeTab == "profile", onClick = onProfile)
            BottomTab(icon = Icons.Rounded.Settings, label = "Налаштування", active = activeTab == "settings", onClick = onSettings)
        }
    }
}

@Composable
private fun BottomTab(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
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
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) BrandGreen else TextSecondary, modifier = Modifier.size(22.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (active) BrandGreen else TextSecondary,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
