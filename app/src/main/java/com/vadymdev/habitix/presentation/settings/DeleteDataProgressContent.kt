package com.vadymdev.habitix.presentation.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun DeleteDataProgressContent(isUk: Boolean, stepIndex: Int) {
    val ringRotation by rememberInfiniteTransition(label = "delete_data_ring").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "delete_data_rotation"
    )

    val steps = listOf(
        if (isUk) "Підготовка очищення..." else "Preparing cleanup...",
        if (isUk) "Видалення локальних даних..." else "Deleting local data...",
        if (isUk) "Видалення хмарних даних..." else "Deleting cloud data...",
        if (isUk) "Завершення..." else "Finalizing..."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .padding(horizontal = 26.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(140.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(126.dp)) {
                val stroke = 6.dp.toPx()
                val diameter = size.minDimension
                val arcSize = Size(diameter - stroke, diameter - stroke)
                val topLeft = Offset(stroke / 2f, stroke / 2f)

                drawArc(
                    color = Color(0xFFA6E9CB),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )

                drawArc(
                    color = BrandGreen,
                    startAngle = ringRotation,
                    sweepAngle = 72f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.start_logo_auth),
                contentDescription = null,
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(34.dp))
        Text(
            text = if (isUk) "Очищаємо ваші дані" else "Cleaning your data",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))

        steps.forEachIndexed { index, text ->
            val isDone = index < stepIndex
            val isActive = index == stepIndex
            RowStep(
                text = text,
                isDone = isDone,
                isActive = isActive
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RowStep(text: String, isDone: Boolean, isActive: Boolean) {
    val bg = when {
        isDone || isActive -> Color(0xFFDDF3E8)
        else -> Color(0xFFEDEEEB)
    }
    val dotColor = when {
        isDone -> BrandGreen
        isActive -> Color(0xFF65CFA2)
        else -> Color(0xFFDCDAD6)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(dotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDone) "✓" else if (isActive) "•" else "+",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(text = text, color = TextSecondary)
        }
    }
}
