package com.vadymdev.habitix.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun HeaderBlock(state: DashboardUiState, isUk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(if (isUk) "Привіт 👋" else "Hi 👋", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Text(
                if (isUk) "Гарного дня!" else "Have a great day!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFDDF2E8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${state.completedCount}/${state.totalCount}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun CalendarStrip(
    pagerState: PagerState,
    anchorWeekStart: LocalDate,
    selected: LocalDate,
    locale: Locale,
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
                        targetValue = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
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
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
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
internal fun BottomBar(
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
            BottomTab(icon = Icons.Rounded.Home, label = if (isUk) "Головна" else "Home", active = activeTab == "home", onClick = onHome)
            BottomTab(icon = Icons.Rounded.Analytics, label = if (isUk) "Статистика" else "Stats", active = activeTab == "stats", onClick = onStats)
            BottomTab(icon = Icons.Rounded.Person, label = if (isUk) "Профіль" else "Profile", active = activeTab == "profile", onClick = onProfile)
            BottomTab(icon = Icons.Rounded.Settings, label = if (isUk) "Налаштування" else "Settings", active = activeTab == "settings", onClick = onSettings)
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
        Icon(icon, contentDescription = label, tint = if (active) MaterialTheme.colorScheme.primary else TextSecondary, modifier = Modifier.size(22.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (active) MaterialTheme.colorScheme.primary else TextSecondary,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
