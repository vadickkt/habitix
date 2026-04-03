package com.vadymdev.habitix.presentation.profile

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.ProfileAchievement
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val categories = listOf("Всі", "Серії", "Час", "Досконалість", "Початок", "Категорії")

@Composable
fun AchievementsScreen(
    state: ProfileUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onSelectCategory: (String) -> Unit
) {
    val isUk = language == AppLanguage.UK
    var selected by remember { mutableStateOf<ProfileAchievement?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E7E3))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = t(isUk, "Назад", "Back"))
            }

            Column {
                Text(t(isUk, "Досягнення", "Achievements"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (isUk) {
                        "${state.unlockedCount}/${state.analytics.allAchievements.size} отримано"
                    } else {
                        "${state.unlockedCount}/${state.analytics.allAchievements.size} unlocked"
                    },
                    color = TextSecondary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatHeadCard(t(isUk, "Досягнень", "Achievements"), state.unlockedCount.toString(), Modifier.weight(1f))
            StatHeadCard(t(isUk, "XP зароблено", "XP earned"), state.analytics.allAchievements.filter { it.unlocked }.sumOf { it.xpReward }.toString(), Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        item {
                            val active = category == state.selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color(0xFFE9E7E3))
                                    .clickable { onSelectCategory(category) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = localizedCategoryLabel(category, isUk),
                                    color = if (active) Color.White else TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            items(state.achievements) { achievement ->
                AchievementRow(achievement = achievement, onClick = { selected = achievement })
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    selected?.let { achievement ->
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text(t(isUk, "Закрити", "Close")) } },
            title = { Text(achievement.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(achievement.description)
                    Text(t(isUk, "Прогрес: ${achievement.progressPercent}%", "Progress: ${achievement.progressPercent}%"))
                    Text(t(isUk, "Нагорода: +${achievement.xpReward} XP", "Reward: +${achievement.xpReward} XP"))
                    Text(
                        if (achievement.unlocked) {
                            t(isUk, "Отримано ${achievement.unlockedDate?.format(formatter).orEmpty()}", "Unlocked ${achievement.unlockedDate?.format(formatter).orEmpty()}")
                        } else {
                            t(isUk, "Ще трохи і ви відкриєте це досягнення", "A bit more to unlock this achievement")
                        },
                        color = if (achievement.unlocked) MaterialTheme.colorScheme.primary else TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun StatHeadCard(title: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(title, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AchievementRow(achievement: ProfileAchievement, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (achievement.unlocked) achievementColor(achievement.colorKey).copy(alpha = 0.35f) else Color(0xFFECEAE6),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievementIcon(achievement.iconKey),
                    contentDescription = null,
                    tint = if (achievement.unlocked) Color.Black else TextSecondary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (achievement.unlocked) Color.Black else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "+${achievement.xpReward} XP",
                        color = if (achievement.unlocked) MaterialTheme.colorScheme.primary else TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .background(
                                if (achievement.unlocked) Color(0xFFDDF3E8) else Color(0xFFEAE7E3),
                                RoundedCornerShape(99.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    achievement.description,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!achievement.unlocked) {
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

private fun achievementIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "flame" -> Icons.Rounded.EmojiEvents
        "medal" -> Icons.Rounded.EmojiEvents
        "crown" -> Icons.Rounded.EmojiEvents
        "zap" -> Icons.Rounded.Star
        "sunrise" -> Icons.Rounded.WbSunny
        "moon" -> Icons.Rounded.Nightlight
        "target" -> Icons.Rounded.Star
        "sparkles" -> Icons.Rounded.Star
        "trophy" -> Icons.Rounded.EmojiEvents
        "heart" -> Icons.Rounded.Star
        "dumbbell" -> Icons.Rounded.FitnessCenter
        "brain" -> Icons.Rounded.Psychology
        "book" -> Icons.Rounded.MenuBook
        "coffee" -> Icons.Rounded.Coffee
        else -> Icons.Rounded.Star
    }
}

private fun achievementColor(colorKey: String): Color {
    return when (colorKey) {
        "peach" -> Color(0xFFF6C491)
        "rose" -> Color(0xFFF0B3C5)
        "mint" -> Color(0xFFB7E9D1)
        "sky" -> Color(0xFFB6DDF1)
        "lavender" -> Color(0xFFD8D0F8)
        else -> Color(0xFFE7E4E0)
    }
}

private fun t(isUk: Boolean, uk: String, en: String): String = if (isUk) uk else en

private fun localizedCategoryLabel(categoryKey: String, isUk: Boolean): String {
    if (isUk) return categoryKey
    return when (categoryKey) {
        "Всі" -> "All"
        "Серії" -> "Streaks"
        "Час" -> "Time"
        "Досконалість" -> "Perfection"
        "Початок" -> "Start"
        "Категорії" -> "Categories"
        else -> categoryKey
    }
}
