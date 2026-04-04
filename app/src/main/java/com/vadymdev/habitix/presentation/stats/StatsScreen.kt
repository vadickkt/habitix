package com.vadymdev.habitix.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.HabitBadge
import com.vadymdev.habitix.domain.model.HabitCategoryStat
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextSecondary

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
                Text(localizedString(isUk, R.string.stats_title_uk, R.string.stats_title_en), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(localizedString(isUk, R.string.stats_subtitle_uk, R.string.stats_subtitle_en), color = TextSecondary)
            }

            item {
                StatsPeriodSelector(
                    selectedDays = state.selectedPeriodDays,
                    isUk = isUk,
                    onSelectPeriod = onSelectPeriod
                )
            }

            item {
                QuickStatsGrid(snapshot = snapshot, isUk = isUk, onMetricClick = onMetricClick)
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
