package com.vadymdev.habitix.presentation.onboarding.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.HabitTemplate
import com.vadymdev.habitix.presentation.components.PrimaryGreenButton
import com.vadymdev.habitix.presentation.components.SelectCircle
import com.vadymdev.habitix.presentation.components.StepIndicator
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun OnboardingHabitsScreen(
    language: AppLanguage,
    habits: List<HabitTemplate>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onComplete: () -> Unit
) {
    val isUk = language == AppLanguage.UK

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            StepIndicator(currentStep = 2, totalSteps = 3)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = t(isUk, "Оберіть перші звички", "Choose your first habits"),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = t(isUk, "Можете обрати декілька для початку", "You can pick several to get started"),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(habits, key = { it.key }) { habit ->
                val isSelected = selected.contains(habit.key)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD7D2CD), RoundedCornerShape(16.dp))
                        .background(Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onToggle(habit.key) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = habit.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.size(14.dp))
                    Text(
                        text = localizedHabitTitle(habit, isUk),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    SelectCircle(isSelected = isSelected)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryGreenButton(
            text = t(isUk, "Завершити", "Finish"),
            enabled = selected.isNotEmpty(),
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun t(isUk: Boolean, uk: String, en: String): String = if (isUk) uk else en

private fun localizedHabitTitle(item: HabitTemplate, isUk: Boolean): String {
    if (isUk) return item.title
    return when (item.key) {
        "water" -> "Drink water"
        "meditation" -> "Meditation"
        "morning" -> "Morning workout"
        "reading" -> "Reading"
        "sleep" -> "Sleep by 11 PM"
        "gratitude" -> "Gratitude"
        else -> item.title
    }
}
