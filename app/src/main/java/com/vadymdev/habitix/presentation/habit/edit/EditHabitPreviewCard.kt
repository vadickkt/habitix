package com.vadymdev.habitix.presentation.habit.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.presentation.habit.create.CreateHabitUiState
import com.vadymdev.habitix.presentation.habit.habitIconVector
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun EditHabitPreviewCard(
    state: CreateHabitUiState,
    previewCardColor: Color,
    heroScale: Float,
    heroAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = heroScale
                scaleY = heroScale
                alpha = heroAlpha
            }
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            stringResource(R.string.edit_habit_preview_title),
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(previewCardColor, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = state.selectedIconKey,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith
                        fadeOut(animationSpec = tween(180))
                },
                label = "edit_preview_icon"
            ) { iconKey ->
                Icon(
                    imageVector = habitIconVector(iconKey),
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Text(
            text = if (state.title.isBlank()) {
                stringResource(R.string.edit_habit_preview_placeholder)
            } else {
                state.title
            },
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
