package com.vadymdev.habitix.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.vadymdev.habitix.R
import com.vadymdev.habitix.ui.theme.TextSecondary
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareProgressDialog(state: ProfileUiState, isUk: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var style by remember { mutableStateOf(ShareStyle.GRADIENT) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(t(isUk, R.string.profile_share_progress_uk, R.string.profile_share_progress_en), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SharePreviewCard(state = state, style = style, isUk = isUk)

            Text(t(isUk, R.string.profile_style_uk, R.string.profile_style_en), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShareStyle.entries.forEach { entry ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(entry.previewBrush())
                            .border(
                                width = 2.dp,
                                color = if (style == entry) Color.Black else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { style = entry }
                    )
                }
            }

            Text(t(isUk, R.string.profile_share_to_uk, R.string.profile_share_to_en), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialItem("IG", "Instagram", Color(0xFFE642A0)) { shareToInstagram(context, state, style, isUk) }
                SocialItem("X", "Twitter", Color(0xFF1D9BF0)) { shareToTwitter(context, state, style, isUk) }
                SocialItem("TG", "Telegram", Color(0xFF2AABEE)) { shareToTelegram(context, state, style, isUk) }
                SocialItem("...", t(isUk, R.string.profile_other_uk, R.string.profile_other_en), Color(0xFFD9D6D2)) { shareImageToAny(context, state, style, isUk) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ShareButton("PNG") { saveShareCardToGallery(context, state, style, isUk) }
                ShareButton(t(isUk, R.string.profile_copy_uk, R.string.profile_copy_en)) { copyLink(context, state, isUk) }
                ShareButton(t(isUk, R.string.profile_share_uk, R.string.profile_share_en)) { shareText(context, state, isUk) }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SocialItem(short: String, name: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(short, color = if (short == "...") TextSecondary else Color.White, fontWeight = FontWeight.Bold)
        }
        Text(name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun SharePreviewCard(state: ProfileUiState, style: ShareStyle, isUk: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(style.previewBrush())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.22f), CircleShape), contentAlignment = Alignment.Center) {
                Text(state.identity.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(state.identity.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                Text(t(isUk, R.string.profile_level_uk, R.string.profile_level_en, state.analytics.level), color = Color.White.copy(alpha = 0.86f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ShareStatCard("${state.analytics.currentStreakDays}", t(isUk, R.string.profile_days_in_row_uk, R.string.profile_days_in_row_en), Modifier.weight(1f))
            ShareStatCard("${state.analytics.bestStreakDays}", t(isUk, R.string.profile_best_streak_short_uk, R.string.profile_best_streak_short_en), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ShareStatCard("${state.analytics.totalCompleted}", t(isUk, R.string.profile_completed_short_uk, R.string.profile_completed_short_en), Modifier.weight(1f))
            ShareStatCard("${state.analytics.daysWithUs}", t(isUk, R.string.profile_days_with_habitix_uk, R.string.profile_days_with_habitix_en), Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Habitix", color = Color.White, fontWeight = FontWeight.Bold)
            androidx.compose.material3.Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.86f))
        }
    }
}

@Composable
private fun ShareStatCard(value: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(10.dp)
    ) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ShareButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFEDEBE7))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
