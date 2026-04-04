package com.vadymdev.habitix.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vadymdev.habitix.domain.model.Habit
import com.vadymdev.habitix.presentation.habit.habitColor
import com.vadymdev.habitix.presentation.habit.habitIcon
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun SwipeableHabitRow(
    habit: Habit,
    isUk: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(dismissState = dismissState, isUk = isUk)
        },
        content = {
            HabitRowCard(
                habit = habit,
                isUk = isUk,
                onToggle = onToggle,
                onDelete = onDelete,
                onEdit = onEdit
            )
        }
    )
}

@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState, isUk: Boolean) {
    val direction = dismissState.dismissDirection
    val bgColor by animateColorAsState(
        targetValue = when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFFDDF3E8)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFFFE3E3)
            else -> Color.Transparent
        },
        label = "swipe_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(if (isUk) "Видалити" else "Delete", color = Color(0xFFD44747), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HabitRowCard(
    habit: Habit,
    isUk: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val done = habit.isCompletedForSelectedDate
    val background by animateColorAsState(if (done) Color(0xFFD8EEE6) else Color(0xFFFFFFFF), label = "habit_bg")
    val border by animateColorAsState(if (done) Color(0xFF87D5B5) else Color(0xFFD8D8D8), label = "habit_border")
    var menuOpened by remember { mutableStateOf(false) }
    val toggleScale by animateFloatAsState(
        targetValue = if (done) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "toggle_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .background(background, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(habitColor(habit.colorKey), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(habitIcon(habit.iconKey), style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = if (isUk) "🔥 ${habit.streakDays} днів" else "🔥 ${habit.streakDays} days",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Дії",
                tint = TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { menuOpened = true }
            )
            DropdownMenu(expanded = menuOpened, onDismissRequest = { menuOpened = false }) {
                DropdownMenuItem(
                    text = { Text(if (isUk) "Редагувати" else "Edit") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuOpened = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (isUk) "Видалити" else "Delete") },
                    onClick = {
                        menuOpened = false
                        onDelete()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))

        Box(
            modifier = Modifier
                .scale(toggleScale)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, if (done) MaterialTheme.colorScheme.primary else Color(0xFFCFCFCF), CircleShape)
                .background(if (done) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = done,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith
                        (fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "habit_toggle"
            ) { checked ->
                Text(
                    text = if (checked) "✓" else "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun AddHabitButton(onCreateHabit: () -> Unit, isUk: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD8D8D8), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCreateHabit)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "+   ${if (isUk) "Додати нову звичку" else "Add a new habit"}",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
