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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.AccentPalette
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun DividerTitle(text: String) {
    Text(
        text,
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
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
internal fun SwitchRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onChecked: (Boolean) -> Unit) {
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
internal fun ClickRow(title: String, subtitle: String, icon: ImageVector, titleColor: Color = TextPrimary, onClick: () -> Unit) {
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
internal fun LanguageItem(flag: String, title: String, selected: Boolean, onClick: () -> Unit) {
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
internal fun ModernBottomBar(
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

internal fun paletteName(value: AccentPalette, isUk: Boolean): String {
    return when (value) {
        AccentPalette.MINT -> if (isUk) "М'ятна" else "Mint"
        AccentPalette.SKY -> if (isUk) "Небесна" else "Sky"
        AccentPalette.LAVENDER -> if (isUk) "Лавандова" else "Lavender"
        AccentPalette.PEACH -> if (isUk) "Персикова" else "Peach"
        AccentPalette.ROSE -> if (isUk) "Рожева" else "Rose"
    }
}

internal fun languageName(value: AppLanguage): String {
    return when (value) {
        AppLanguage.UK -> "Українська"
        AppLanguage.EN -> "English"
    }
}

internal fun paletteColor(value: AccentPalette): Color {
    return when (value) {
        AccentPalette.MINT -> Color(0xFF08B46D)
        AccentPalette.SKY -> Color(0xFF15A4CC)
        AccentPalette.LAVENDER -> Color(0xFF8B7FD8)
        AccentPalette.PEACH -> Color(0xFFE09B57)
        AccentPalette.ROSE -> Color(0xFFE0708C)
    }
}
