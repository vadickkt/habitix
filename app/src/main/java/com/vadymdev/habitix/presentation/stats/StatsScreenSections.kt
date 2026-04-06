package com.vadymdev.habitix.presentation.stats

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.rememberScrollState
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.domain.model.HabitStatsSnapshot
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun StatsPeriodSelector(selectedDays: Int, isUk: Boolean, onSelectPeriod: (Int) -> Unit) {
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
                        7 -> localizedString(isUk, R.string.stats_period_7_uk, R.string.stats_period_7_en)
                        30 -> localizedString(isUk, R.string.stats_period_30_uk, R.string.stats_period_30_en)
                        else -> localizedString(isUk, R.string.stats_period_90_uk, R.string.stats_period_90_en)
                    },
                    color = if (active) Color.White else TextSecondary,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
internal fun QuickStatsGrid(snapshot: HabitStatsSnapshot, isUk: Boolean, onMetricClick: (StatsMetric) -> Unit) {
    val cards = listOf(
        StatCardData("🔥", snapshot.longestStreak.toString(), localizedString(isUk, R.string.stats_longest_streak_uk, R.string.stats_longest_streak_en), StatsMetric.LONGEST_STREAK),
        StatCardData("🏆", snapshot.earnedBadgesCount.toString(), localizedString(isUk, R.string.stats_badges_earned_uk, R.string.stats_badges_earned_en), StatsMetric.BADGES),
        StatCardData("◎", "${snapshot.successRatePercent}%", localizedString(isUk, R.string.stats_success_rate_uk, R.string.stats_success_rate_en), StatsMetric.SUCCESS),
        StatCardData("↗", snapshot.completedTasksCount.toString(), localizedString(isUk, R.string.stats_tasks_completed_uk, R.string.stats_tasks_completed_en), StatsMetric.COMPLETED)
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
internal fun HeatmapCard(levels: List<Int>, isUk: Boolean, onDayClick: (Int) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val columns = levels.chunked(7)
    val heatmapScrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(localizedString(isUk, R.string.stats_activity_uk, R.string.stats_activity_en), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(heatmapScrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            columns.forEachIndexed { columnIndex, weekColumn ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { rowIndex ->
                        val index = columnIndex * 7 + rowIndex
                        val level = weekColumn.getOrNull(rowIndex)
                        if (level == null) {
                            Spacer(modifier = Modifier.size(13.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .background(heatmapColor(level, primary), CircleShape)
                                    .clickable { onDayClick(index) }
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(localizedString(isUk, R.string.stats_less_uk, R.string.stats_less_en), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(6.dp))
            listOf(0, 1, 2, 3, 4).map { heatmapColor(it, primary) }.forEach { c ->
                Box(modifier = Modifier.padding(horizontal = 1.dp).size(9.dp).background(c, CircleShape))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(localizedString(isUk, R.string.stats_more_uk, R.string.stats_more_en), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun CategoryCard(categories: List<HabitCategoryStat>, isUk: Boolean, onCategoryClick: (HabitCategoryStat) -> Unit) {
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
        Text(localizedString(isUk, R.string.stats_by_category_uk, R.string.stats_by_category_en), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

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
internal fun BadgesCard(badges: List<HabitBadge>, isUk: Boolean, onBadgeClick: (HabitBadge) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val badgeRows = badges.chunked(3)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(localizedString(isUk, R.string.achievements_title_uk, R.string.achievements_title_en), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            badgeRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { badge ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp)
                                .background(if (badge.earned) primary.copy(alpha = 0.12f) else Color(0xFFF0EFEC), RoundedCornerShape(14.dp))
                                .clickable { onBadgeClick(badge) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                        ) {
                            Text(badge.emoji, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                localizedBadgeTitle(badge, isUk),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (badge.earned) TextPrimary else TextSecondary,
                                fontWeight = if (badge.earned) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 2
                            )
                        }
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatsBottomBar(
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
            BottomItem(Icons.Rounded.Home, localizedString(isUk, R.string.nav_home_uk, R.string.nav_home_en), activeTab == "home", onHome)
            BottomItem(Icons.Rounded.Analytics, localizedString(isUk, R.string.nav_stats_uk, R.string.nav_stats_en), activeTab == "stats", onStats)
            BottomItem(Icons.Rounded.Person, localizedString(isUk, R.string.nav_profile_uk, R.string.nav_profile_en), activeTab == "profile", onProfile)
            BottomItem(Icons.Rounded.Settings, localizedString(isUk, R.string.nav_settings_uk, R.string.nav_settings_en), activeTab == "settings", onSettings)
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

private data class StatCardData(
    val emoji: String,
    val value: String,
    val title: String,
    val metric: StatsMetric
)

private fun heatmapColor(level: Int, primary: Color): Color {
    return when (level) {
        0 -> Color(0xFFE6E3DF)
        1 -> primary.copy(alpha = 0.25f)
        2 -> primary.copy(alpha = 0.45f)
        3 -> primary.copy(alpha = 0.7f)
        else -> primary
    }
}
