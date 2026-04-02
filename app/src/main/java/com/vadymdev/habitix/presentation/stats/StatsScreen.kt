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
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    state: StatsUiState,
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
                Text("Статистика", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ваш прогрес та досягнення", color = TextSecondary)
            }

            item {
                StatsPeriodSelector(
                    selectedDays = state.selectedPeriodDays,
                    onSelectPeriod = onSelectPeriod
                )
            }

            item {
                QuickStatsGrid(snapshot, onMetricClick = onMetricClick)
            }

            item {
                HeatmapCard(
                    levels = snapshot.heatmapLevels,
                    onDayClick = onHeatmapDayClick
                )
            }

            item {
                CategoryCard(
                    categories = snapshot.categoryStats,
                    onCategoryClick = onCategoryClick
                )
            }

            item {
                BadgesCard(
                    badges = snapshot.badges,
                    onBadgeClick = onBadgeClick
                )
            }
        }

        StatsBottomBar(
            onHome = onOpenDashboard,
            onStats = {},
            onProfile = onOpenProfile,
            onSettings = onOpenSettings,
            activeTab = "stats"
        )
    }

    state.selectedMetric?.let { metric ->
        MetricDetailsDialog(metric = metric, snapshot = snapshot, onDismiss = onMetricDismiss)
    }

    state.selectedCategory?.let { category ->
        CategoryDetailsDialog(category = category, onDismiss = onCategoryDismiss)
    }

    state.selectedBadge?.let { badge ->
        BadgeDetailsDialog(badge = badge, onDismiss = onBadgeDismiss)
    }

    state.selectedHeatmapDay?.let { day ->
        HeatmapDayDialog(day = day, onDismiss = onHeatmapDayDismiss)
    }
}

@Composable
private fun StatsPeriodSelector(selectedDays: Int, onSelectPeriod: (Int) -> Unit) {
    val options = listOf(7, 30, 90)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { days ->
            val active = selectedDays == days
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) BrandGreen else Color.White)
                    .border(1.dp, if (active) BrandGreen else Color(0xFFDCDCDC), RoundedCornerShape(999.dp))
                    .clickable { onSelectPeriod(days) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (days) {
                        7 -> "7 днів"
                        30 -> "30 днів"
                        else -> "90 днів"
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
                .background(Color(0xFFDDF2E8), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(card.emoji) }
        Text(card.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(card.title, color = TextSecondary)
    }
}

@Composable
private fun HeatmapCard(levels: List<Int>, onDayClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Активність", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(15) { week ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { day ->
                        val index = week * 7 + day
                        val level = levels.getOrElse(index) { 0 }
                        val color = when (level) {
                            0 -> Color(0xFFE6E3DF)
                            1 -> Color(0xFFCFEFE2)
                            2 -> Color(0xFFA7E3CA)
                            3 -> Color(0xFF67D09E)
                            else -> BrandGreen
                        }
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
            Text("Менше", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(6.dp))
            listOf(Color(0xFFE6E3DF), Color(0xFFCFEFE2), Color(0xFFA7E3CA), Color(0xFF67D09E), BrandGreen).forEach { c ->
                Box(modifier = Modifier.padding(horizontal = 1.dp).size(9.dp).background(c, CircleShape))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("Більше", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CategoryCard(categories: List<HabitCategoryStat>, onCategoryClick: (HabitCategoryStat) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("За категоріями", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        categories.forEach { cat ->
            val fillColor = when (cat.colorKey) {
                "mint" -> Color(0xFF67D09E)
                "orange" -> Color(0xFFE6B07A)
                "blue" -> Color(0xFF6FC8E7)
                "purple" -> Color(0xFFB5AEEF)
                else -> BrandGreen
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCategoryClick(cat) }
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cat.name)
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
private fun BadgesCard(badges: List<HabitBadge>, onBadgeClick: (HabitBadge) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Досягнення", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(180.dp)
        ) {
            items(badges) { badge ->
                Column(
                    modifier = Modifier
                        .background(if (badge.earned) Color(0xFFEAF5EF) else Color(0xFFF0EFEC), RoundedCornerShape(14.dp))
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
private fun MetricDetailsDialog(metric: StatsMetric, snapshot: HabitStatsSnapshot, onDismiss: () -> Unit) {
    val (title, body) = when (metric) {
        StatsMetric.LONGEST_STREAK -> "Найдовша серія" to "Ваш рекорд без пропусків: ${snapshot.longestStreak} дн."
        StatsMetric.BADGES -> "Бейджі" to "Відкрито бейджів: ${snapshot.earnedBadgesCount} із ${snapshot.badges.size}."
        StatsMetric.SUCCESS -> "Успішність" to "Відсоток виконання звичок у вибраному періоді: ${snapshot.successRatePercent}%."
        StatsMetric.COMPLETED -> "Виконано" to "Завдань завершено за період: ${snapshot.completedTasksCount}."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Зрозуміло") } },
        title = { Text(title) },
        text = { Text(body) }
    )
}

@Composable
private fun CategoryDetailsDialog(category: HabitCategoryStat, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Ок") } },
        title = { Text(category.name) },
        text = { Text("Частка категорії у ваших виконаннях: ${category.percent}%.") }
    )
}

@Composable
private fun BadgeDetailsDialog(badge: HabitBadge, onDismiss: () -> Unit) {
    val status = if (badge.earned) "Бейдж відкрито" else "Бейдж ще не відкрито"
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрити") } },
        title = { Text("${badge.emoji} ${badge.title}") },
        text = { Text(status) }
    )
}

@Composable
private fun HeatmapDayDialog(day: StatsHeatmapDayDetails, onDismiss: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрити") } },
        title = { Text("День активності") },
        text = { Text("${day.date.format(formatter)}\nВиконаних звичок: ${day.completedCount}") }
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
            BottomItem(Icons.Rounded.Home, "Головна", activeTab == "home", onHome)
            BottomItem(Icons.Rounded.Analytics, "Статистика", activeTab == "stats", onStats)
            BottomItem(Icons.Rounded.Person, "Профіль", activeTab == "profile", onProfile)
            BottomItem(Icons.Rounded.Settings, "Налаштування", activeTab == "settings", onSettings)
        }
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Color(0xFFE7F8EF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) BrandGreen else TextSecondary)
        Text(label, color = if (active) BrandGreen else TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
