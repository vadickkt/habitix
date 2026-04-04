package com.vadymdev.habitix.presentation.settings

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactSupport
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.ui.theme.AppBackground
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
    onDeleteData: () -> Unit,
    onResetDeleteDataState: () -> Unit
) {
    val context = LocalContext.current
    var showColorSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showDangerDialog by remember { mutableStateOf<String?>(null) }

    val settings = state.settings
    val isUk = settings.language == AppLanguage.UK
    fun t(uk: String, en: String): String = if (isUk) uk else en

    if (state.deleteData.phase == DeleteDataPhase.RUNNING) {
        DeleteDataProgressContent(isUk = isUk, stepIndex = state.deleteData.stepIndex)
        return
    }

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
                ClickRow(t("Видалити дані", "Delete data"), t("Очистити локальні та хмарні дані", "Clear local and cloud data"), Icons.Rounded.DeleteForever, titleColor = Color(0xFFE24949)) { showDangerDialog = "delete" }
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
        SettingsColorSheet(
            isUk = isUk,
            settings = settings,
            onAccent = onAccent,
            onDismiss = { showColorSheet = false }
        )
    }

    if (showLanguageSheet) {
        SettingsLanguageSheet(
            isUk = isUk,
            settings = settings,
            onLanguage = onLanguage,
            onDismiss = { showLanguageSheet = false }
        )
    }

    if (showDangerDialog != null) {
        DangerActionDialog(
            isUk = isUk,
            action = showDangerDialog.orEmpty(),
            onDismiss = { showDangerDialog = null },
            onConfirm = {
                val action = showDangerDialog
                showDangerDialog = null
                if (action == "logout") onSignOut() else onDeleteData()
            }
        )
    }

    if (state.deleteData.phase == DeleteDataPhase.SUCCESS) {
        DeleteDataSuccessDialog(isUk = isUk, onDismiss = onResetDeleteDataState)
    }

    if (state.deleteData.phase == DeleteDataPhase.ERROR) {
        DeleteDataErrorDialog(
            isUk = isUk,
            errorMessage = state.deleteData.errorMessage,
            onDismiss = onResetDeleteDataState
        )
    }
}
