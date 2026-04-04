package com.vadymdev.habitix.presentation.auth

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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.R
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary

private const val LOADING_STEP_COUNT = 4

@Composable
fun AuthLoadingContent(
    currentStepIndex: Int,
    isUk: Boolean
) {
    val ringRotation by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
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
            CircularProgressRing(rotation = ringRotation)

            Image(
                painter = painterResource(id = R.drawable.start_logo_auth),
                contentDescription = "Mascot",
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(34.dp))

        val steps = authLoadingSteps(currentStepIndex, isUk)
        steps.forEachIndexed { index, step ->
            LoadingStepRow(
                text = step.text,
                status = step.status,
                isMutedRow = index == LOADING_STEP_COUNT - 1
            )
            Spacer(modifier = Modifier.height(if (index == LOADING_STEP_COUNT - 1) 0.dp else 10.dp))
        }
    }
}

@Composable
private fun CircularProgressRing(rotation: Float) {
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
            startAngle = rotation,
            sweepAngle = 72f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

private data class LoadingStepModel(
    val text: String,
    val status: LoadingStepStatus
)

private enum class LoadingStepStatus {
    COMPLETED,
    ACTIVE,
    PENDING
}

@Composable
private fun authLoadingSteps(currentStepIndex: Int, isUk: Boolean): List<LoadingStepModel> {
    val _unused = isUk
    val titles = listOf(
        stringResource(R.string.auth_loading_connecting_google),
        stringResource(R.string.auth_loading_fetching_profile),
        stringResource(R.string.auth_loading_setting_up_account),
        stringResource(R.string.auth_loading_almost_done)
    )

    return titles.mapIndexed { index, title ->
        val status = when {
            currentStepIndex < 0 -> LoadingStepStatus.PENDING
            currentStepIndex >= titles.size -> LoadingStepStatus.COMPLETED
            index < currentStepIndex -> LoadingStepStatus.COMPLETED
            index == currentStepIndex -> LoadingStepStatus.ACTIVE
            else -> LoadingStepStatus.PENDING
        }
        LoadingStepModel(text = title, status = status)
    }
}

@Composable
private fun LoadingStepRow(text: String, status: LoadingStepStatus, isMutedRow: Boolean) {
    val bgColor = when {
        isMutedRow -> Color.Transparent
        status == LoadingStepStatus.PENDING -> Color(0xFFEDEEEB)
        else -> Color(0xFFDDF3E8)
    }

    val textColor = when (status) {
        LoadingStepStatus.PENDING -> if (isMutedRow) Color(0xFFCECCC8) else Color(0xFF8D938E)
        LoadingStepStatus.ACTIVE -> TextPrimary
        LoadingStepStatus.COMPLETED -> TextPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepStateIcon(status = status, muted = isMutedRow)
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}

@Composable
private fun StepStateIcon(status: LoadingStepStatus, muted: Boolean) {
    val color = when (status) {
        LoadingStepStatus.COMPLETED -> BrandGreen
        LoadingStepStatus.ACTIVE -> Color(0xFF65CFA2)
        LoadingStepStatus.PENDING -> Color(0xFFDCDAD6)
    }

    val symbol = when (status) {
        LoadingStepStatus.COMPLETED -> "✓"
        LoadingStepStatus.ACTIVE -> "•"
        LoadingStepStatus.PENDING -> "+"
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = when (status) {
                LoadingStepStatus.PENDING -> Color(0xFFECEAE6)
                else -> Color.White
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
