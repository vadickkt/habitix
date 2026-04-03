package com.vadymdev.habitix.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.ProfileAchievement
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    language: AppLanguage,
    onUpdateName: (String) -> Unit,
    onUpdateBio: (String) -> Unit,
    onUpdateAvatar: (String?) -> Unit,
    onOpenAllAchievements: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val isUk = language == AppLanguage.UK
    var showShare by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(false) }
    var editBio by remember { mutableStateOf(false) }
    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var showAvatarActions by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var draftName by remember(state.identity.displayName) { mutableStateOf(state.identity.displayName) }
    var draftBio by remember(state.identity.bio) { mutableStateOf(state.identity.bio) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            persistReadPermissionIfPossible(context, uri)
            val localUri = createCircularAvatarUri(context, uri)
            onUpdateAvatar(localUri?.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val sourceUri = pendingCameraUri
            val localUri = sourceUri?.let { createCircularAvatarUri(context, it) }
            onUpdateAvatar(localUri?.toString())
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, t(isUk, "Потрібен дозвіл на камеру", "Camera permission is required"), Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = t(isUk, "Профіль", "Profile"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                ProfileHeader(
                    state = state,
                    onEditAvatar = { showAvatarActions = true },
                    onEditName = { editName = true },
                    onEditBio = { editBio = true }
                )
            }

            item { ProfileStatsGrid(state) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Досягнення", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = t(isUk, "Всі", "All"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onOpenAllAchievements)
                    )
                }
            }

            items(state.analytics.topAchievements.size) { index ->
                val achievement = state.analytics.topAchievements[index]
                AchievementCard(achievement = achievement, compact = true)
            }

            item {
                MonthActivityCard(state)
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showShare = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t(isUk, "Поділитися прогресом", "Share progress"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        ProfileBottomBar(
            onHome = onOpenDashboard,
            onStats = onOpenStats,
            onProfile = {},
            onSettings = onOpenSettings,
            isUk = isUk,
            activeTab = "profile"
        )
    }

    if (showShare) {
        ShareProgressDialog(state = state, onDismiss = { showShare = false })
    }

    if (showAvatarActions) {
        AlertDialog(
            onDismissRequest = { showAvatarActions = false },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showAvatarActions = false
                }) {
                    Text(t(isUk, "Галерея", "Gallery"))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permissionState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                    if (permissionState == PackageManager.PERMISSION_GRANTED) {
                        val uri = createTempImageUri(context)
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    showAvatarActions = false
                }) {
                    Text(t(isUk, "Камера", "Camera"))
                }
            },
            title = { Text(t(isUk, "Оберіть аватар", "Choose avatar")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t(isUk, "Фото зберігається локально на пристрої.", "Photo is stored locally on the device."))
                    TextButton(onClick = {
                        onUpdateAvatar(null)
                        showAvatarActions = false
                    }) {
                        Text(t(isUk, "Скинути аватар", "Reset avatar"))
                    }
                }
            }
        )
    }

    if (editName) {
        EditTextDialog(
            title = t(isUk, "Ім'я", "Name"),
            value = draftName,
            onValueChange = { draftName = it },
            error = nameValidationError,
            onDismiss = { editName = false },
            onConfirm = {
                val trimmed = draftName.trim()
                when {
                    trimmed.isBlank() -> nameValidationError = t(isUk, "Ім'я не може бути порожнім", "Name cannot be empty")
                    trimmed.length < 2 -> nameValidationError = t(isUk, "Ім'я має містити щонайменше 2 символи", "Name must have at least 2 characters")
                    else -> {
                        nameValidationError = null
                        onUpdateName(trimmed)
                        editName = false
                    }
                }
            }
        )
    }

    if (editBio) {
        EditTextDialog(
            title = t(isUk, "Опис", "Bio"),
            value = draftBio,
            onValueChange = { draftBio = it },
            onDismiss = { editBio = false },
            onConfirm = {
                onUpdateBio(draftBio)
                editBio = false
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileUiState,
    onEditAvatar: () -> Unit,
    onEditName: () -> Unit,
    onEditBio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F4EE), RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFC6E8DA), CircleShape)
                .border(3.dp, Color.White, CircleShape)
                .clickable(onClick = onEditAvatar),
            contentAlignment = Alignment.Center
        ) {
            if (state.identity.avatarUri != null) {
                AsyncImage(
                    model = state.identity.avatarUri,
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            } else {
                Text(state.identity.avatarInitials, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(state.identity.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Rounded.Create,
                contentDescription = "Редагувати ім'я",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp).clickable(onClick = onEditName)
            )
        }

        Text(
            text = state.identity.bio,
            color = TextSecondary,
            modifier = Modifier.clickable(onClick = onEditBio)
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFD5F0E2))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Рівень ${state.analytics.level}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFAEDFC8))
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
private fun ProfileStatsGrid(state: ProfileUiState) {
    val cards = listOf(
        Triple("Поточна серія", "${state.analytics.currentStreakDays} днів", Icons.Rounded.EmojiEvents),
        Triple("Найкраща серія", "${state.analytics.bestStreakDays} днів", Icons.Rounded.EmojiEvents),
        Triple("Загалом виконано", state.analytics.totalCompleted.toString(), Icons.Rounded.Star),
        Triple("Днів з нами", state.analytics.daysWithUs.toString(), Icons.Rounded.CalendarToday)
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

@Composable
private fun AchievementCard(achievement: ProfileAchievement, compact: Boolean) {
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
                    Text(achievement.title, fontWeight = FontWeight.SemiBold)
                    if (achievement.unlocked) {
                        Text(
                            "Отримано",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .background(Color(0xFFDDF3E8), RoundedCornerShape(99.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(achievement.description, color = TextSecondary)
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
private fun MonthActivityCard(state: ProfileUiState) {
    val max = state.analytics.monthWeeklyActivity.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Активність цього місяця", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(
                "${if (state.analytics.monthGrowthPercent >= 0) "+" else ""}${state.analytics.monthGrowthPercent}%",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text("порівняно з минулим місяцем", color = TextSecondary)
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
                        .background(Color(0xFFB9E3D2))
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Тиж 1", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text("Тиж 2", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text("Тиж 3", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text("Тиж 4", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareProgressDialog(state: ProfileUiState, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var style by remember { mutableStateOf(ShareStyle.GRADIENT) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Поділитися прогресом", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SharePreviewCard(state = state, style = style)

            Text("Стиль", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
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

            Text("Поділитися в", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialItem("IG", "Instagram", Color(0xFFE642A0)) { shareToInstagram(context, state, style) }
                SocialItem("X", "Twitter", Color(0xFF1D9BF0)) { shareToTwitter(context, state, style) }
                SocialItem("TG", "Telegram", Color(0xFF2AABEE)) { shareToTelegram(context, state, style) }
                SocialItem("...", "Інше", Color(0xFFD9D6D2)) { shareImageToAny(context, state, style) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ShareButton("PNG") { saveShareCardToGallery(context, state, style) }
                ShareButton("Скопіювати") { copyLink(context, state) }
                ShareButton("Поділитися") { shareText(context, state) }
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
private fun SharePreviewCard(state: ProfileUiState, style: ShareStyle) {
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
                Text("Рівень ${state.analytics.level}", color = Color.White.copy(alpha = 0.86f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ShareStatCard("${state.analytics.currentStreakDays}", "днів поспіль", Modifier.weight(1f))
            ShareStatCard("${state.analytics.bestStreakDays}", "найкраща серія", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ShareStatCard("${state.analytics.totalCompleted}", "виконано", Modifier.weight(1f))
            ShareStatCard("${state.analytics.daysWithUs}", "днів з Habitix", Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Habitix", color = Color.White, fontWeight = FontWeight.Bold)
            Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.86f))
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

private enum class ShareStyle {
    GRADIENT,
    DARK,
    LIGHT
}

private fun ShareStyle.previewBrush(): Brush {
    return when (this) {
        ShareStyle.GRADIENT -> Brush.linearGradient(listOf(Color(0xFF07BA73), Color(0xFFDFC169)))
        ShareStyle.DARK -> Brush.linearGradient(listOf(Color(0xFF21233C), Color(0xFF181A2E)))
        ShareStyle.LIGHT -> Brush.linearGradient(listOf(Color(0xFFE9F6EF), Color(0xFFF6F5F2)))
    }
}

@Composable
private fun ProfileBottomBar(
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Color(0xFFE7F8EF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) MaterialTheme.colorScheme.primary else TextSecondary)
        Text(label, color = if (active) MaterialTheme.colorScheme.primary else TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

private fun t(isUk: Boolean, uk: String, en: String): String = if (isUk) uk else en

@Composable
private fun EditTextDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("Зберегти") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } },
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

private fun copyLink(context: Context, state: ProfileUiState) {
    val text = buildShareText(state)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("progress", text))
    Toast.makeText(context, "Скопійовано", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, state: ProfileUiState) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareText(state))
    }
    context.startActivity(Intent.createChooser(shareIntent, "Поділитися прогресом"))
}

private fun shareToInstagram(context: Context, state: ProfileUiState, style: ShareStyle) {
    shareToPackage(
        context = context,
        packageName = "com.instagram.android",
        fallbackUrl = "https://instagram.com",
        state = state,
        style = style
    )
}

private fun shareToTwitter(context: Context, state: ProfileUiState, style: ShareStyle) {
    shareToPackage(
        context = context,
        packageName = "com.twitter.android",
        fallbackUrl = "https://x.com",
        state = state,
        style = style
    )
}

private fun shareToTelegram(context: Context, state: ProfileUiState, style: ShareStyle) {
    shareToPackage(
        context = context,
        packageName = "org.telegram.messenger",
        fallbackUrl = "https://t.me",
        state = state,
        style = style
    )
}

private fun shareImageToAny(context: Context, state: ProfileUiState, style: ShareStyle) {
    val uri = createShareImageUri(context, state, style)
    if (uri == null) {
        Toast.makeText(context, "Не вдалося підготувати зображення", Toast.LENGTH_SHORT).show()
        return
    }

    val text = buildShareText(state)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Поділитися прогресом"))
}

private fun shareToPackage(
    context: Context,
    packageName: String,
    fallbackUrl: String,
    state: ProfileUiState,
    style: ShareStyle
) {
    val uri = createShareImageUri(context, state, style)
    if (uri == null) {
        Toast.makeText(context, "Не вдалося підготувати зображення", Toast.LENGTH_SHORT).show()
        return
    }

    val text = buildShareText(state)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage(packageName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
        }
    }
}

private fun createShareImageUri(context: Context, state: ProfileUiState, style: ShareStyle): Uri? {
    return runCatching {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val shareFile = File(shareDir, "share_${System.currentTimeMillis()}.png")
        val bitmap = renderShareBitmap(state, style)
        FileOutputStream(shareFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
    }.getOrNull()
}

private fun persistReadPermissionIfPossible(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File(imageDir, "avatar_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun createCircularAvatarUri(context: Context, sourceUri: Uri): Uri? {
    return runCatching {
        val sourceBitmap = decodeBitmapFromUri(context, sourceUri) ?: return null
        val size = minOf(sourceBitmap.width, sourceBitmap.height)
        val left = (sourceBitmap.width - size) / 2
        val top = (sourceBitmap.height - size) / 2

        val squared = Bitmap.createBitmap(sourceBitmap, left, top, size, size)
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }

        canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), maskPaint)
        canvas.drawBitmap(squared, Rect(0, 0, size, size), Rect(0, 0, size, size), imagePaint)

        val avatarsDir = File(context.filesDir, "avatars").apply { mkdirs() }
        val avatarFile = File(avatarsDir, "avatar_${System.currentTimeMillis()}.png")
        FileOutputStream(avatarFile).use { output ->
            result.compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        sourceBitmap.recycle()
        squared.recycle()
        result.recycle()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", avatarFile)
    }.getOrNull()
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    }.getOrNull()
}

private fun saveShareCardToGallery(context: Context, state: ProfileUiState, style: ShareStyle) {
    runCatching {
        val bitmap = renderShareBitmap(state, style)
        val fileName = "habitix_share_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver

        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Habitix")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values) ?: error("Не вдалося створити файл")

        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: error("Не вдалося записати PNG")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
    }.onSuccess {
        Toast.makeText(context, "PNG збережено у Галерею", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, it.message ?: "Помилка збереження PNG", Toast.LENGTH_SHORT).show()
    }
}

private fun renderShareBitmap(state: ProfileUiState, style: ShareStyle): Bitmap {
    val width = 1080
    val height = 1350
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val background = Paint().apply {
        shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            style.gradientColors(),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 56f, 56f, background)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 54f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#EAF5EF")
        textSize = 34f
    }
    canvas.drawText(state.identity.displayName, 88f, 130f, titlePaint)
    canvas.drawText("Рівень ${state.analytics.level}", 88f, 180f, subtitlePaint)

    drawStatBlock(canvas, 80f, 250f, 430f, 240f, "${state.analytics.currentStreakDays}", "днів поспіль")
    drawStatBlock(canvas, 570f, 250f, 430f, 240f, "${state.analytics.bestStreakDays}", "найкраща серія")
    drawStatBlock(canvas, 80f, 540f, 430f, 240f, "${state.analytics.totalCompleted}", "виконано")
    drawStatBlock(canvas, 570f, 540f, 430f, 240f, "${state.analytics.daysWithUs}", "днів з Habitix")

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 38f
        isFakeBoldText = true
    }
    canvas.drawText("Habitix", 88f, 1260f, footerPaint)

    return bitmap
}

private fun drawStatBlock(
    canvas: Canvas,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    value: String,
    label: String
) {
    val block = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(45, 255, 255, 255)
    }
    canvas.drawRoundRect(left, top, left + width, top + height, 36f, 36f, block)

    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 62f
        isFakeBoldText = true
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#EAF5EF")
        textSize = 32f
    }

    canvas.drawText(value, left + 40f, top + 118f, valuePaint)
    canvas.drawText(label, left + 40f, top + 176f, labelPaint)
}

private fun ShareStyle.gradientColors(): IntArray {
    return when (this) {
        ShareStyle.GRADIENT -> intArrayOf(android.graphics.Color.parseColor("#07BA73"), android.graphics.Color.parseColor("#DFC169"))
        ShareStyle.DARK -> intArrayOf(android.graphics.Color.parseColor("#21233C"), android.graphics.Color.parseColor("#181A2E"))
        ShareStyle.LIGHT -> intArrayOf(android.graphics.Color.parseColor("#E9F6EF"), android.graphics.Color.parseColor("#F6F5F2"))
    }
}

private fun buildShareText(state: ProfileUiState): String {
    return """
        Мій прогрес у Habitix:
        Рівень ${state.analytics.level} (${state.analytics.xpCurrent}/${state.analytics.xpTarget} XP)
        Поточна серія: ${state.analytics.currentStreakDays} днів
        Найкраща серія: ${state.analytics.bestStreakDays} днів
        Всього виконано: ${state.analytics.totalCompleted}
    """.trimIndent()
}
