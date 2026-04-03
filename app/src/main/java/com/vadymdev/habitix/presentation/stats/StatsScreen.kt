package com.vadymdev.habitix.presentation.stats

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    state: StatsUiState,
    language: AppLanguage,
    onSelectPeriod: (Int) -> Unit,
    onMetricClick: (StatsMetric) -> Unit,
    onMetricDismiss: () -> Unit,
    onCategoryClick: (HabitCategoryStat) -> Unit,
    onCategoryDismiss: () -> Unit,
    onBadgeClick: (HabitBadge) -> Unit,
    onBadgeDismiss: () -> Unit,
    onHeatmapDayClick: (Int) -> Unit,
    onHeatmapDayDismiss: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isUk = language == AppLanguage.UK
    val snapshot = state.snapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(t(isUk, "Статистика", "Statistics"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(t(isUk, "Ваш прогрес та досягнення", "Your progress and achievements"), color = TextSecondary)
            }

            item {
                StatsPeriodSelector(
                    selectedDays = state.selectedPeriodDays,
                    isUk = isUk,
                    onSelectPeriod = onSelectPeriod
                )
            }

            item {
                QuickStatsGrid(snapshot, onMetricClick = onMetricClick)
            }

            item {
                HeatmapCard(
                    levels = snapshot.heatmapLevels,
                    isUk = isUk,
                    onDayClick = onHeatmapDayClick
                )
            }

            item {
                CategoryCard(
                    categories = snapshot.categoryStats,
                    isUk = isUk,
                    onCategoryClick = onCategoryClick
                )
            }

            item {
                BadgesCard(
                    badges = snapshot.badges,
                    isUk = isUk,
                    onBadgeClick = onBadgeClick
                )
            }
        }

        StatsBottomBar(
            onHome = onOpenDashboard,
            onStats = {},
            onProfile = onOpenProfile,
            onSettings = onOpenSettings,
            isUk = isUk,
            activeTab = "stats"
        )
    }

    state.selectedMetric?.let { metric ->
        MetricDetailsDialog(metric = metric, snapshot = snapshot, isUk = isUk, onDismiss = onMetricDismiss)
    }

    state.selectedCategory?.let { category ->
        CategoryDetailsDialog(category = category, isUk = isUk, onDismiss = onCategoryDismiss)
    }

    state.selectedBadge?.let { badge ->
        BadgeDetailsDialog(badge = badge, isUk = isUk, onDismiss = onBadgeDismiss)
    }

    state.selectedHeatmapDay?.let { day ->
        HeatmapDayDialog(day = day, isUk = isUk, onDismiss = onHeatmapDayDismiss)
    }
}

@Composable
private fun StatsPeriodSelector(selectedDays: Int, isUk: Boolean, onSelectPeriod: (Int) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val options = listOf(7, 30, 90)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { days ->
            val active = selectedDays == days
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) primary else Color.White)
                    .border(1.dp, if (active) primary else Color(0xFFDCDCDC), RoundedCornerShape(999.dp))
                    .clickable { onSelectPeriod(days) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (days) {
                        7 -> if (isUk) "7 днів" else "7 days"
                        30 -> if (isUk) "30 днів" else "30 days"
                        else -> if (isUk) "90 днів" else "90 days"
                    },
                    color = if (active) Color.White else TextSecondary,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(snapshot: HabitStatsSnapshot, onMetricClick: (StatsMetric) -> Unit) {
    val cards = listOf(
        StatCardData("🔥", snapshot.longestStreak.toString(), "Найдовша серія", StatsMetric.LONGEST_STREAK),
        StatCardData("🏆", snapshot.earnedBadgesCount.toString(), "Бейджів отримано", StatsMetric.BADGES),
        StatCardData("◎", "${snapshot.successRatePercent}%", "Успішність", StatsMetric.SUCCESS),
        StatCardData("↗", snapshot.completedTasksCount.toString(), "Виконано завдань", StatsMetric.COMPLETED)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(Modifier.weight(1f), cards[0], onClick = { onMetricClick(cards[0].metric) })
            StatCard(Modifier.weight(1f), cards[1], onClick = { onMetricClick(cards[1].metric) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(Modifier.weight(1f), cards[2], onClick = { onMetricClick(cards[2].metric) })
            StatCard(Modifier.weight(1f), cards[3], onClick = { onMetricClick(cards[3].metric) })
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, card: StatCardData, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(primary.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(card.emoji) }
        Text(card.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(card.title, color = TextSecondary)
    }
}

@Composable
private fun HeatmapCard(levels: List<Int>, isUk: Boolean, onDayClick: (Int) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(t(isUk, "Активність", "Activity"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(15) { week ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { day ->
                        val index = week * 7 + day
                        val level = levels.getOrElse(index) { 0 }
                        val color = heatmapColor(level, primary)
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .background(color, CircleShape)
                                .clickable { onDayClick(index) }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(t(isUk, "Менше", "Less"), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(6.dp))
            listOf(0, 1, 2, 3, 4).map { heatmapColor(it, primary) }.forEach { c ->
                Box(modifier = Modifier.padding(horizontal = 1.dp).size(9.dp).background(c, CircleShape))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(t(isUk, "Більше", "More"), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CategoryCard(categories: List<HabitCategoryStat>, isUk: Boolean, onCategoryClick: (HabitCategoryStat) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(t(isUk, "За категоріями", "By category"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        categories.forEach { cat ->
            val fillColor = when (cat.colorKey) {
                "mint" -> primary
                "orange" -> Color(0xFFE6B07A)
                "blue" -> Color(0xFF6FC8E7)
                "purple" -> Color(0xFFB5AEEF)
                else -> primary
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCategoryClick(cat) }
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(localizedCategoryName(cat.name, isUk))
                    Text("${cat.percent}%", fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFFE7E3DE))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(cat.percent / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(fillColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgesCard(badges: List<HabitBadge>, isUk: Boolean, onBadgeClick: (HabitBadge) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(t(isUk, "Досягнення", "Achievements"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(180.dp)
        ) {
            items(badges) { badge ->
                Column(
                    modifier = Modifier
                        .background(if (badge.earned) primary.copy(alpha = 0.12f) else Color(0xFFF0EFEC), RoundedCornerShape(14.dp))
                        .clickable { onBadgeClick(badge) }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(badge.emoji, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        badge.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (badge.earned) TextPrimary else TextSecondary,
                        fontWeight = if (badge.earned) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricDetailsDialog(metric: StatsMetric, snapshot: HabitStatsSnapshot, isUk: Boolean, onDismiss: () -> Unit) {
    val (title, body) = when (metric) {
        StatsMetric.LONGEST_STREAK -> t(isUk, "Найдовша серія", "Longest streak") to t(
            isUk,
            "Ваш рекорд без пропусків: ${snapshot.longestStreak} дн.",
            "Your best no-break streak: ${snapshot.longestStreak} days."
        )
        StatsMetric.BADGES -> t(isUk, "Бейджі", "Badges") to t(
            isUk,
            "Відкрито бейджів: ${snapshot.earnedBadgesCount} із ${snapshot.badges.size}.",
            "Unlocked badges: ${snapshot.earnedBadgesCount} of ${snapshot.badges.size}."
        )
        StatsMetric.SUCCESS -> t(isUk, "Успішність", "Success rate") to t(
            isUk,
            "Відсоток виконання звичок у вибраному періоді: ${snapshot.successRatePercent}%.",
            "Habit completion rate for selected period: ${snapshot.successRatePercent}%."
        )
        StatsMetric.COMPLETED -> t(isUk, "Виконано", "Completed") to t(
            isUk,
            "Завдань завершено за період: ${snapshot.completedTasksCount}.",
            "Tasks completed in this period: ${snapshot.completedTasksCount}."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(t(isUk, "Зрозуміло", "Got it")) } },
        title = { Text(title) },
        text = { Text(body) }
    )
}

@Composable
private fun CategoryDetailsDialog(category: HabitCategoryStat, isUk: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(if (isUk) "Ок" else "OK") } },
        title = { Text(localizedCategoryName(category.name, isUk)) },
        text = { Text(t(isUk, "Частка категорії у ваших виконаннях: ${category.percent}%.", "Category share in your completions: ${category.percent}%.") ) }
    )
}

@Composable
private fun BadgeDetailsDialog(badge: HabitBadge, isUk: Boolean, onDismiss: () -> Unit) {
    val status = if (badge.earned) t(isUk, "Бейдж відкрито", "Badge unlocked") else t(isUk, "Бейдж ще не відкрито", "Badge is not unlocked yet")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(t(isUk, "Закрити", "Close")) } },
        title = { Text("${badge.emoji} ${badge.title}") },
        text = { Text(status) }
    )
}

@Composable
private fun HeatmapDayDialog(day: StatsHeatmapDayDetails, isUk: Boolean, onDismiss: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(t(isUk, "Закрити", "Close")) } },
        title = { Text(t(isUk, "День активності", "Activity day")) },
        text = { Text(t(isUk, "${day.date.format(formatter)}\nВиконаних звичок: ${day.completedCount}", "${day.date.format(formatter)}\nCompleted habits: ${day.completedCount}")) }
    )
}

private data class StatCardData(
    val emoji: String,
    val value: String,
    val title: String,
    val metric: StatsMetric
)

@Composable
private fun StatsBottomBar(
    onHome: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    isUk: Boolean,
    activeTab: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8E8)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomItem(Icons.Rounded.Home, t(isUk, "Головна", "Home"), activeTab == "home", onHome)
            BottomItem(Icons.Rounded.Analytics, t(isUk, "Статистика", "Stats"), activeTab == "stats", onStats)
            BottomItem(Icons.Rounded.Person, t(isUk, "Профіль", "Profile"), activeTab == "profile", onProfile)
            BottomItem(Icons.Rounded.Settings, t(isUk, "Налаштування", "Settings"), activeTab == "settings", onSettings)
        }
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) primary.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) MaterialTheme.colorScheme.primary else TextSecondary)
        Text(label, color = if (active) MaterialTheme.colorScheme.primary else TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

private fun heatmapColor(level: Int, primary: Color): Color {
    return when (level) {
        0 -> Color(0xFFE6E3DF)
        1 -> primary.copy(alpha = 0.25f)
        2 -> primary.copy(alpha = 0.45f)
        3 -> primary.copy(alpha = 0.7f)
        else -> primary
    }
}

private fun t(isUk: Boolean, uk: String, en: String): String = if (isUk) uk else en

private fun localizedCategoryName(raw: String, isUk: Boolean): String {
    if (isUk) return raw
    return when (raw) {
        "Здоров'я" -> "Health"
        "Продуктивність" -> "Productivity"
        "Спорт" -> "Sport"
        "Усвідомленість" -> "Mindfulness"
        else -> raw
    }
}
