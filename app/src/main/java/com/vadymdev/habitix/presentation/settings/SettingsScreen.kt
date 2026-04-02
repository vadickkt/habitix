package com.vadymdev.habitix.presentation.settings

import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.domain.model.ThemeMode
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onOpenDashboard: () -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onAccent: (AccentPalette) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onPushToggle: (Boolean) -> Unit,
    onTimePicked: (Int, Int) -> Unit,
    onSoundsToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(t("Налаштування", "Settings"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, start = 4.dp))
            Spacer(modifier = Modifier.height(8.dp))
            DividerTitle(t("ВИГЛЯД", "APPEARANCE"))
            SettingsCard {
                SwitchRow(
                    title = t("Темна тема", "Dark theme"),
                    subtitle = if (settings.themeMode == ThemeMode.DARK) t("Увімкнено", "Enabled") else t("Вимкнено", "Disabled"),
                    icon = "☼",
                    checked = settings.themeMode == ThemeMode.DARK,
                    onChecked = onThemeToggle
                )
                ClickRow(t("Кольорова тема", "Color theme"), paletteName(settings.accentPalette, isUk), "◍") { showColorSheet = true }
                ClickRow(t("Мова", "Language"), languageName(settings.language), "◎") { showLanguageSheet = true }
            }
        }

        item {
            DividerTitle(t("СПОВІЩЕННЯ", "NOTIFICATIONS"))
            SettingsCard {
                SwitchRow(t("Push-сповіщення", "Push notifications"), t("Нагадування про звички", "Habit reminders"), "◔", settings.pushEnabled, onPushToggle)
                ClickRow(t("Час нагадувань", "Reminder time"), "%02d:%02d".format(settings.reminderHour, settings.reminderMinute), "◷") {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onTimePicked(hour, minute) },
                        settings.reminderHour,
                        settings.reminderMinute,
                        true
                    ).show()
                }
                SwitchRow(t("Звуки", "Sounds"), t("Звуки при виконанні", "Completion sounds"), "◍", settings.soundsEnabled, onSoundsToggle)
                SwitchRow(t("Вібрація", "Vibration"), t("Тактильний відгук", "Haptic feedback"), "◉", settings.vibrationEnabled, onVibrationToggle)
            }
        }

        item {
            DividerTitle(t("АКАУНТ ТА БЕЗПЕКА", "ACCOUNT & SECURITY"))
            SettingsCard {
                SwitchRow("Біометрія", "Face ID / Touch ID", "⌂", settings.biometricEnabled, onBiometricToggle)
                ClickRow(t("Змінити пароль", "Change password"), "", "⌘", onClick = {})
                SwitchRow(t("Синхронізація", "Sync"), t("Автоматично синхронізувати", "Sync automatically"), "☁", settings.autoSyncEnabled, onAutoSyncToggle)
            }
        }

        item {
            DividerTitle(t("ПІДТРИМКА", "SUPPORT"))
            SettingsCard {
                ClickRow(t("Довідка", "Help"), t("FAQ та інструкції", "FAQ and guides"), "?", onClick = {})
                ClickRow(t("Зв'язатися з нами", "Contact us"), t("Підтримка та пропозиції", "Support and ideas"), "◫", onClick = {})
                ClickRow(t("Оцінити додаток", "Rate app"), t("Залиште відгук", "Leave feedback"), "☆", onClick = {})
            }
        }

        item {
            DividerTitle(t("ПРАВОВА ІНФОРМАЦІЯ", "LEGAL"))
            SettingsCard {
                ClickRow(t("Умови використання", "Terms of use"), "", "◬", onClick = {})
                ClickRow(t("Політика конфіденційності", "Privacy policy"), "", "◯", onClick = {})
            }
        }

        item {
            DividerTitle(t("НЕБЕЗПЕЧНА ЗОНА", "DANGER ZONE"))
            SettingsCard {
                ClickRow(t("Вийти", "Sign out"), "", "⇥", titleColor = Color(0xFFE24949)) { showDangerDialog = "logout" }
                ClickRow(t("Видалити акаунт", "Delete account"), t("Це видалить всі ваші дані", "This will remove all your data"), "⌦", titleColor = Color(0xFFE24949)) { showDangerDialog = "delete" }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ModernBottomBar(onSettings = {}, onHome = onOpenDashboard, isUk = isUk)
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
    Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
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
private fun SwitchRow(title: String, subtitle: String, icon: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon)
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) Text(subtitle, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun ClickRow(title: String, subtitle: String, icon: String, titleColor: Color = TextPrimary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon)
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) Text(subtitle, color = TextSecondary)
        }
        Text(if (subtitle.isNotBlank()) subtitle else "›", color = TextSecondary)
    }
}

@Composable
private fun IconBubble(icon: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color(0xFFF0EFEC), CircleShape),
        contentAlignment = Alignment.Center
    ) { Text(icon, color = TextSecondary) }
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
private fun ModernBottomBar(onSettings: () -> Unit, onHome: () -> Unit, isUk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8E8))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomItem("⌂", if (isUk) "Головна" else "Home", false, onHome)
        BottomItem("⌁", if (isUk) "Статистика" else "Stats", false, {})
        BottomItem("◡", if (isUk) "Профіль" else "Profile", false, {})
        BottomItem("⚙", if (isUk) "Налаштування" else "Settings", true, onSettings)
    }
}

@Composable
private fun BottomItem(icon: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(if (active) Color(0xFFE7F8EF) else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, color = if (active) BrandGreen else TextSecondary)
        Text(label, color = if (active) BrandGreen else TextSecondary)
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
