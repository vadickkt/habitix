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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vadymdev.habitix.R
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun ProfileHeader(
    state: ProfileUiState,
    isUk: Boolean,
    onEditAvatar: () -> Unit,
    onEditName: () -> Unit,
    onEditBio: () -> Unit,
    onAvatarBroken: () -> Unit
) {
    var avatarLoadFailed by remember(state.identity.avatarUri) { mutableStateOf(false) }
    val avatarModel = remember(state.identity.avatarUri) {
        resolveAvatarModel(state.identity.avatarUri)
    }
    val avatarStatusText = when {
        state.isAvatarUpdating -> t(isUk, R.string.profile_avatar_updating_uk, R.string.profile_avatar_updating_en)
        avatarLoadFailed -> t(isUk, R.string.profile_avatar_corrupted_uk, R.string.profile_avatar_corrupted_en)
        state.identity.avatarUri.isNullOrBlank() -> t(isUk, R.string.profile_avatar_tap_to_add_uk, R.string.profile_avatar_tap_to_add_en)
        else -> t(isUk, R.string.profile_avatar_saved_local_uk, R.string.profile_avatar_saved_local_en)
    }
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(avatarLoadFailed, state.identity.avatarUri) {
        if (avatarLoadFailed && !state.identity.avatarUri.isNullOrBlank()) {
            onAvatarBroken()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primary.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(primary.copy(alpha = 0.26f), CircleShape)
                .border(3.dp, Color.White, CircleShape)
                .clickable(onClick = onEditAvatar),
            contentAlignment = Alignment.Center
        ) {
            if (avatarModel != null && !avatarLoadFailed) {
                AsyncImage(
                    model = avatarModel,
                    contentDescription = t(isUk, R.string.profile_avatar_uk, R.string.profile_avatar_en),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    onError = {
                        avatarLoadFailed = true
                    }
                )
                if (state.isAvatarUpdating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Text(state.identity.avatarInitials, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(state.identity.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Rounded.Create,
                contentDescription = t(isUk, R.string.profile_edit_name_uk, R.string.profile_edit_name_en),
                tint = TextSecondary,
                modifier = Modifier.size(16.dp).clickable(onClick = onEditName)
            )
        }

        Text(
            text = state.identity.bio,
            color = TextSecondary,
            modifier = Modifier.clickable(onClick = onEditBio)
        )
        Text(
            text = avatarStatusText,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(primary.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(t(isUk, R.string.profile_level_uk, R.string.profile_level_en, state.analytics.level), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primary.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((state.analytics.xpCurrent.toFloat() / state.analytics.xpTarget.toFloat()).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("${state.analytics.xpCurrent}/${state.analytics.xpTarget} XP", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
internal fun ProfileStatsGrid(state: ProfileUiState, isUk: Boolean) {
    val cards = listOf(
        Triple(
            t(isUk, R.string.profile_current_streak_uk, R.string.profile_current_streak_en),
            t(isUk, R.string.profile_days_count_uk, R.string.profile_days_count_en, state.analytics.currentStreakDays),
            Icons.Rounded.EmojiEvents
        ),
        Triple(
            t(isUk, R.string.profile_best_streak_uk, R.string.profile_best_streak_en),
            t(isUk, R.string.profile_days_count_uk, R.string.profile_days_count_en, state.analytics.bestStreakDays),
            Icons.Rounded.EmojiEvents
        ),
        Triple(t(isUk, R.string.profile_total_completed_uk, R.string.profile_total_completed_en), state.analytics.totalCompleted.toString(), Icons.Rounded.Star),
        Triple(t(isUk, R.string.profile_days_with_us_uk, R.string.profile_days_with_us_en), state.analytics.daysWithUs.toString(), Icons.Rounded.CalendarToday)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard(Modifier.weight(1f), cards[0])
            InfoCard(Modifier.weight(1f), cards[1])
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard(Modifier.weight(1f), cards[2])
            InfoCard(Modifier.weight(1f), cards[3])
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier, value: Triple<String, String, ImageVector>) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(value.third, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(value.first, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(value.second, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
