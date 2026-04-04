package com.vadymdev.habitix.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.ui.theme.BrandGreen

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .size(width = if (isActive) 28.dp else 8.dp, height = 6.dp)
                    .background(
                        color = if (isActive) BrandGreen else Color(0xFFE3E3E3),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Composable
fun PrimaryGreenButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandGreen,
            contentColor = Color.White,
            disabledContainerColor = BrandGreen.copy(alpha = 0.55f),
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SelectCircle(isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = if (isSelected) BrandGreen else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color(0xFFC9C9C9),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Text(
                text = "✓",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
