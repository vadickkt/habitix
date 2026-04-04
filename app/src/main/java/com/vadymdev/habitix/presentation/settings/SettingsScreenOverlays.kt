package com.vadymdev.habitix.presentation.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.AppSettings
import com.vadymdev.habitix.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsColorSheet(
    isUk: Boolean,
    settings: AppSettings,
    onAccent: (AccentPalette) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(st(isUk, "Кольорова тема", "Color theme"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                                onDismiss()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsLanguageSheet(
    isUk: Boolean,
    settings: AppSettings,
    onLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(st(isUk, "Мова", "Language"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LanguageItem("🇺🇦", "Українська", settings.language == AppLanguage.UK) {
                onLanguage(AppLanguage.UK)
                onDismiss()
            }
            LanguageItem("🇬🇧", "English", settings.language == AppLanguage.EN) {
                onLanguage(AppLanguage.EN)
                onDismiss()
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
internal fun DangerActionDialog(
    isUk: Boolean,
    action: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (action == "logout") st(isUk, "Вийти?", "Sign out?") else st(isUk, "Видалити всі дані?", "Delete all data?")) },
        text = {
            Text(
                if (action == "logout") {
                    st(isUk, "Ви зможете увійти знову через Google.", "You can sign in again with Google.")
                } else {
                    st(
                        isUk,
                        "Буде очищено локальні та хмарні дані. Акаунт залишиться активним.",
                        "Local and cloud data will be removed. Your account will remain active."
                    )
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(st(isUk, "Підтвердити", "Confirm")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(st(isUk, "Скасувати", "Cancel")) }
        }
    )
}

@Composable
internal fun DeleteDataSuccessDialog(isUk: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(st(isUk, "Готово", "Done")) },
        text = { Text(st(isUk, "Дані успішно очищено.", "Data was successfully cleared.")) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(st(isUk, "Добре", "OK"))
            }
        }
    )
}

@Composable
internal fun DeleteDataErrorDialog(isUk: Boolean, errorMessage: String?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(st(isUk, "Помилка", "Error")) },
        text = { Text(errorMessage ?: st(isUk, "Не вдалося видалити дані", "Failed to delete data")) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(st(isUk, "Закрити", "Close"))
            }
        }
    )
}

private fun st(isUk: Boolean, uk: String, en: String): String = if (isUk) uk else en
