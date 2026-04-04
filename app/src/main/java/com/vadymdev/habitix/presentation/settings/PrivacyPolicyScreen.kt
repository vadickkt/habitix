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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun PrivacyPolicyScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val _unusedLanguage = language

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
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = TextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(stringResource(R.string.privacy_updated_date), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Person,
                title = stringResource(R.string.privacy_section_1_title),
                content = stringResource(R.string.privacy_section_1_content)
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Sync,
                title = stringResource(R.string.privacy_section_2_title),
                content = stringResource(R.string.privacy_section_2_content)
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Notifications,
                title = stringResource(R.string.privacy_section_3_title),
                content = stringResource(R.string.privacy_section_3_content)
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Lock,
                title = stringResource(R.string.privacy_section_4_title),
                content = stringResource(R.string.privacy_section_4_content)
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Schedule,
                title = stringResource(R.string.privacy_section_5_title),
                content = stringResource(R.string.privacy_section_5_content)
            )
        }

        item {
            PolicySection(
                icon = Icons.Rounded.Security,
                title = stringResource(R.string.privacy_section_6_title),
                content = stringResource(R.string.privacy_section_6_content)
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
