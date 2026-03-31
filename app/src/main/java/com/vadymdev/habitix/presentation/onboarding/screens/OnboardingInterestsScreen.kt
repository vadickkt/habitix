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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vadymdev.habitix.domain.model.InterestCategory
import com.vadymdev.habitix.presentation.components.PrimaryGreenButton
import com.vadymdev.habitix.presentation.components.StepIndicator
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun OnboardingInterestsScreen(
    interests: List<InterestCategory>,
    selectedKeys: Set<String>,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit
) {
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
            text = "Оберіть сфери інтересів",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Що ви хочете покращити?",
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
                            Text(text = item.emoji, fontSize = 25.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = item.title,
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
            text = "Далі",
            enabled = selectedKeys.isNotEmpty(),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
