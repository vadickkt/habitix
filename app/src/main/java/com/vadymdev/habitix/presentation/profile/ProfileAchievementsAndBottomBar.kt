package com.vadymdev.habitix.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.ProfileAchievement
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun AchievementCard(achievement: ProfileAchievement, compact: Boolean, isUk: Boolean) {
    val title = localizedAchievementTitle(achievement, isUk)
    val description = localizedAchievementDescription(achievement, isUk)
    val iconTint = if (achievement.unlocked) TextPrimary else TextSecondary
    val bg = if (achievement.unlocked) achievementColor(achievement.colorKey).copy(alpha = 0.35f) else Color(0xFFEBE9E6)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(46.dp).background(bg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(achievementIcon(achievement.iconKey), contentDescription = null, tint = iconTint)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    if (achievement.unlocked) {
                        Text(
                            t(isUk, R.string.profile_unlocked_uk, R.string.profile_unlocked_en),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(99.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(description, color = TextSecondary)
            }
        }

        if (!achievement.unlocked && !compact) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFE5E2DE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(achievement.progressPercent / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("${achievement.progressPercent}%", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun MonthActivityCard(state: ProfileUiState, isUk: Boolean) {
    val max = state.analytics.monthWeeklyActivity.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(t(isUk, R.string.profile_month_activity_uk, R.string.profile_month_activity_en), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(
                "${if (state.analytics.monthGrowthPercent >= 0) "+" else ""}${state.analytics.monthGrowthPercent}%",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(t(isUk, R.string.profile_compared_last_month_uk, R.string.profile_compared_last_month_en), color = TextSecondary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            state.analytics.monthWeeklyActivity.forEach { value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((24 + (value * 60 / max)).dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(t(isUk, R.string.profile_week_1_uk, R.string.profile_week_1_en), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(t(isUk, R.string.profile_week_2_uk, R.string.profile_week_2_en), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(t(isUk, R.string.profile_week_3_uk, R.string.profile_week_3_en), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(t(isUk, R.string.profile_week_4_uk, R.string.profile_week_4_en), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun ProfileBottomBar(
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
            BottomItem(Icons.Rounded.Home, t(isUk, R.string.nav_home_uk, R.string.nav_home_en), activeTab == "home", onHome)
            BottomItem(Icons.Rounded.Analytics, t(isUk, R.string.nav_stats_uk, R.string.nav_stats_en), activeTab == "stats", onStats)
            BottomItem(Icons.Rounded.Person, t(isUk, R.string.nav_profile_uk, R.string.nav_profile_en), activeTab == "profile", onProfile)
            BottomItem(Icons.Rounded.Settings, t(isUk, R.string.nav_settings_uk, R.string.nav_settings_en), activeTab == "settings", onSettings)
        }
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) MaterialTheme.colorScheme.primary else TextSecondary)
        Text(label, color = if (active) MaterialTheme.colorScheme.primary else TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun EditTextDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    confirmText: String,
    dismissText: String,
    error: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                isError = error != null,
                singleLine = true
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(error, color = Color(0xFFD44747), style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}
