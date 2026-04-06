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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Psychology
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
import androidx.compose.ui.unit.sp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.domain.model.InterestCategory
import com.vadymdev.habitix.presentation.components.PrimaryGreenButton
import com.vadymdev.habitix.presentation.components.StepIndicator
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun OnboardingInterestsScreen(
    language: AppLanguage,
    interests: List<InterestCategory>,
    selectedKeys: Set<String>,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit
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
            StepIndicator(currentStep = 1, totalSteps = 3)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(if (isUk) R.string.onboarding_interests_title_uk else R.string.onboarding_interests_title_en),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(if (isUk) R.string.onboarding_interests_subtitle_uk else R.string.onboarding_interests_subtitle_en),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(interests, key = { it.key }) { item ->
                val isSelected = selectedKeys.contains(item.key)
                val activeTint = Color(item.cardColor)

                Box(
                    modifier = Modifier
                        .height(124.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandGreen else Color(0xFFD7D2CD),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = if (isSelected) Color(0xFFE5F4ED) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onToggle(item.key) }
                        .padding(10.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .background(BrandGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(activeTint, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = interestIcon(item.key),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = localizedInterestTitle(item.key, isUk),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryGreenButton(
            text = stringResource(if (isUk) R.string.onboarding_next_uk else R.string.onboarding_next_en),
            enabled = selectedKeys.isNotEmpty(),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun localizedInterestTitle(key: String, isUk: Boolean): String {
    return when (key) {
        "health" -> stringResource(if (isUk) R.string.interest_health_uk else R.string.interest_health_en)
        "productivity" -> stringResource(if (isUk) R.string.interest_productivity_uk else R.string.interest_productivity_en)
        "sport" -> stringResource(if (isUk) R.string.interest_sport_uk else R.string.interest_sport_en)
        "mindfulness" -> stringResource(if (isUk) R.string.interest_mindfulness_uk else R.string.interest_mindfulness_en)
        else -> key
    }
}

private fun interestIcon(key: String): ImageVector {
    return when (key) {
        "health" -> Icons.Rounded.Favorite
        "productivity" -> Icons.Rounded.Bolt
        "sport" -> Icons.Rounded.FitnessCenter
        "mindfulness" -> Icons.Rounded.Psychology
        else -> Icons.Rounded.Favorite
    }
}
