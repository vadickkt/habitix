package com.vadymdev.habitix.presentation.settings

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.ContactSupport
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onOpenDashboard: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenProfile: () -> Unit,
    onAccent: (AccentPalette) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onPushToggle: (Boolean) -> Unit,
    onTimePicked: (Int, Int) -> Unit,
    onSoundsToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val context = LocalContext.current
    var showColorSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showDangerDialog by remember { mutableStateOf<String?>(null) }

    val settings = state.settings
    val isUk = settings.language == AppLanguage.UK
    fun t(uk: String, en: String): String = if (isUk) uk else en

    fun openContact() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@habitix.app")
            putExtra(Intent.EXTRA_SUBJECT, "Habitix support")
        }
        context.startActivity(intent)
    }

    fun openRateApp() {
        val appPackage = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackage"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackage"))

        try {
            context.startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .padding(horizontal = 14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                t("Налаштування", "Settings"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, start = 2.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            DividerTitle(t("ВИГЛЯД", "APPEARANCE"))
            SettingsCard {
                ClickRow(t("Кольорова тема", "Color theme"), paletteName(settings.accentPalette, isUk), Icons.Rounded.Palette) { showColorSheet = true }
                ClickRow(t("Мова", "Language"), languageName(settings.language), Icons.Rounded.Language) { showLanguageSheet = true }
            }
        }

        item {
            DividerTitle(t("СПОВІЩЕННЯ", "NOTIFICATIONS"))
            SettingsCard {
                SwitchRow(t("Push-сповіщення", "Push notifications"), t("Нагадування про звички", "Habit reminders"), Icons.Rounded.NotificationsActive, settings.pushEnabled, onPushToggle)
                if (settings.pushEnabled) {
                    ClickRow(t("Час нагадувань", "Reminder time"), "%02d:%02d".format(settings.reminderHour, settings.reminderMinute), Icons.Rounded.Schedule) {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> onTimePicked(hour, minute) },
                            settings.reminderHour,
                            settings.reminderMinute,
                            true
                        ).show()
                    }
                    SwitchRow(t("Звуки", "Sounds"), t("Звуки при виконанні", "Completion sounds"), Icons.Rounded.VolumeUp, settings.soundsEnabled, onSoundsToggle)
                    SwitchRow(t("Вібрація", "Vibration"), t("Тактильний відгук", "Haptic feedback"), Icons.Rounded.Vibration, settings.vibrationEnabled, onVibrationToggle)
                }
            }
        }

        item {
            DividerTitle(t("АКАУНТ ТА БЕЗПЕКА", "ACCOUNT & SECURITY"))
            SettingsCard {
                SwitchRow(t("Синхронізація", "Sync"), t("Автоматично синхронізувати", "Sync automatically"), Icons.Rounded.Sync, settings.autoSyncEnabled, onAutoSyncToggle)
            }
        }

        item {
            DividerTitle(t("ПІДТРИМКА", "SUPPORT"))
            SettingsCard {
                ClickRow(t("Зв'язатися з нами", "Contact us"), t("Підтримка та пропозиції", "Support and ideas"), Icons.Rounded.ContactSupport, onClick = ::openContact)
                ClickRow(t("Оцінити додаток", "Rate app"), t("Залиште відгук", "Leave feedback"), Icons.Rounded.RateReview, onClick = ::openRateApp)
            }
        }

        item {
            DividerTitle(t("ПРАВОВА ІНФОРМАЦІЯ", "LEGAL"))
            SettingsCard {
                ClickRow(t("Політика конфіденційності", "Privacy policy"), t("Як ми обробляємо дані", "How we process your data"), Icons.Rounded.Policy, onClick = onOpenPrivacyPolicy)
            }
        }

        item {
            DividerTitle(t("НЕБЕЗПЕЧНА ЗОНА", "DANGER ZONE"))
            SettingsCard {
                ClickRow(t("Вийти", "Sign out"), "", Icons.Rounded.Logout, titleColor = Color(0xFFE24949)) { showDangerDialog = "logout" }
                ClickRow(t("Видалити акаунт", "Delete account"), t("Це видалить всі ваші дані", "This will remove all your data"), Icons.Rounded.DeleteForever, titleColor = Color(0xFFE24949)) { showDangerDialog = "delete" }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ModernBottomBar(
                onSettings = {},
                onHome = onOpenDashboard,
                onStats = onOpenStats,
                onProfile = onOpenProfile,
                isUk = isUk
            )
        }
    }

    if (showColorSheet) {
        ModalBottomSheet(onDismissRequest = { showColorSheet = false }) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(t("Кольорова тема", "Color theme"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    AccentPalette.entries.forEach { palette ->
                        val active = settings.accentPalette == palette
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .background(paletteColor(palette), RoundedCornerShape(16.dp))
                                .border(if (active) 2.dp else 0.dp, BrandGreen, RoundedCornerShape(16.dp))
                                .clickable {
                                    onAccent(palette)
                                    showColorSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (active) Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showLanguageSheet) {
        ModalBottomSheet(onDismissRequest = { showLanguageSheet = false }) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t("Мова", "Language"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LanguageItem("🇺🇦", "Українська", settings.language == AppLanguage.UK) {
                    onLanguage(AppLanguage.UK)
                    showLanguageSheet = false
                }
                LanguageItem("🇬🇧", "English", settings.language == AppLanguage.EN) {
                    onLanguage(AppLanguage.EN)
                    showLanguageSheet = false
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showDangerDialog != null) {
        AlertDialog(
            onDismissRequest = { showDangerDialog = null },
            title = { Text(if (showDangerDialog == "logout") t("Вийти?", "Sign out?") else t("Видалити акаунт?", "Delete account?")) },
            text = { Text(if (showDangerDialog == "logout") t("Ви зможете увійти знову через Google.", "You can sign in again with Google.") else t("Цю дію неможливо скасувати.", "This action cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    val action = showDangerDialog
                    showDangerDialog = null
                    if (action == "logout") onSignOut() else onDeleteAccount()
                }) { Text(t("Підтвердити", "Confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { showDangerDialog = null }) { Text(t("Скасувати", "Cancel")) }
            }
        )
    }
}

@Composable
private fun DividerTitle(text: String) {
    Text(
        text,
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
    ) {
        content()
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onChecked: (Boolean) -> Unit) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChecked(it)
            }
        )
    }
}

@Composable
private fun ClickRow(title: String, subtitle: String, icon: ImageVector, titleColor: Color = TextPrimary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Text("›", color = TextSecondary)
    }
}

@Composable
private fun IconBubble(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color(0xFFF0EFEC), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LanguageItem(flag: String, title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFFDDF3E8) else Color(0xFFF0EFEC), RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) BrandGreen else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag)
        Spacer(modifier = Modifier.size(10.dp))
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        if (selected) Text("✓", color = BrandGreen, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModernBottomBar(
    onSettings: () -> Unit,
    onHome: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    isUk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomItem(Modifier.weight(1f), Icons.Rounded.Home, if (isUk) "Головна" else "Home", false, onHome)
        BottomItem(Modifier.weight(1f), Icons.Rounded.Analytics, if (isUk) "Статистика" else "Stats", false, onStats)
        BottomItem(Modifier.weight(1f), Icons.Rounded.Person, if (isUk) "Профіль" else "Profile", false, onProfile)
        BottomItem(Modifier.weight(1f), Icons.Rounded.Settings, if (isUk) "Налаштування" else "Settings", true, onSettings)
    }
}

@Composable
private fun BottomItem(modifier: Modifier, icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .background(if (active) Color(0xFFE7F8EF) else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) BrandGreen else TextSecondary, modifier = Modifier.size(21.dp))
        Text(
            text = label,
            color = if (active) BrandGreen else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun paletteName(value: AccentPalette, isUk: Boolean): String {
    return when (value) {
        AccentPalette.MINT -> if (isUk) "М'ятна" else "Mint"
        AccentPalette.SKY -> if (isUk) "Небесна" else "Sky"
        AccentPalette.LAVENDER -> if (isUk) "Лавандова" else "Lavender"
        AccentPalette.PEACH -> if (isUk) "Персикова" else "Peach"
        AccentPalette.ROSE -> if (isUk) "Рожева" else "Rose"
    }
}

private fun languageName(value: AppLanguage): String {
    return when (value) {
        AppLanguage.UK -> "Українська"
        AppLanguage.EN -> "English"
    }
}

private fun paletteColor(value: AccentPalette): Color {
    return when (value) {
        AccentPalette.MINT -> Color(0xFF08B46D)
        AccentPalette.SKY -> Color(0xFF15A4CC)
        AccentPalette.LAVENDER -> Color(0xFF8B7FD8)
        AccentPalette.PEACH -> Color(0xFFE09B57)
        AccentPalette.ROSE -> Color(0xFFE0708C)
    }
}
