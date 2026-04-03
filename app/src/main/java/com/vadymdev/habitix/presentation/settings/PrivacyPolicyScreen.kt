package com.vadymdev.habitix.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun PrivacyPolicyScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val isUk = language == AppLanguage.UK

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFF1F1F1), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = TextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(t(isUk, "Політика конфіденційності", "Privacy Policy"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(t(isUk, "Оновлено: 02.04.2026", "Updated: 02.04.2026"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Person,
                title = t(isUk, "1. Які дані ми зберігаємо", "1. What data we store"),
                content = t(
                    isUk,
                    "Ми зберігаємо дані профілю, звички, відмітки виконання та налаштування. Для режиму гостя інформація зберігається лише локально на вашому пристрої.",
                    "We store profile data, habits, completion marks, and settings. In guest mode, data is stored only locally on your device."
                )
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Sync,
                title = t(isUk, "2. Синхронізація та хмара", "2. Sync and cloud"),
                content = t(
                    isUk,
                    "Якщо ви входите через Google, дані можуть синхронізуватися з Firebase для відновлення після перевстановлення додатку або входу на новому пристрої.",
                    "If you sign in with Google, data can sync with Firebase to restore after reinstalling or signing in on a new device."
                )
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Notifications,
                title = t(isUk, "3. Сповіщення", "3. Notifications"),
                content = t(
                    isUk,
                    "Нагадування працюють лише коли перемикач Push-сповіщень увімкнено в налаштуваннях. Після вимкнення нові нагадування не плануються, а активні скасовуються.",
                    "Reminders work only when Push notifications are enabled in settings. After disabling, new reminders are not scheduled and active ones are canceled."
                )
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Lock,
                title = t(isUk, "4. Безпека", "4. Security"),
                content = t(
                    isUk,
                    "Передача даних до хмарних сервісів відбувається через захищені канали. Ми не продаємо ваші персональні дані третім сторонам.",
                    "Data transfer to cloud services uses secure channels. We do not sell your personal data to third parties."
                )
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Schedule,
                title = t(isUk, "5. Зберігання та видалення", "5. Storage and deletion"),
                content = t(
                    isUk,
                    "Ви можете видалити акаунт у налаштуваннях. Локальні дані режиму гостя можна очистити, видаливши додаток або через очищення даних системою Android.",
                    "You can delete your account in settings. Guest mode local data can be cleared by uninstalling the app or clearing app data in Android settings."
                )
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Security,
                title = t(isUk, "6. Ваші права", "6. Your rights"),
                content = t(
                    isUk,
                    "Ви можете переглядати, змінювати і видаляти свої дані в будь-який момент. Користуючись додатком, ви погоджуєтесь із цією політикою конфіденційності.",
                    "You can view, update, and delete your data at any time. By using the app, you agree to this privacy policy."
                )
            )
        }
    }
}

@Composable
private fun PolicySection(icon: ImageVector, title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF0EFEC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(content, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

private fun t(isUk: Boolean, uk: String, en: String): String = if (isUk) uk else en
