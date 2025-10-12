package com.msa.calendar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.msa.calendar.ui.DatePickerColors

@Composable
fun RowScope.CancelActionButton(
    text: String,
    colors: DatePickerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.weight(1f),
    shape: Shape = RoundedCornerShape(18.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "cancelElevation",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 0.5f,
        animationSpec = tween(durationMillis = 160),
        label = "cancelBorder",
    )
    val glow by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 0.45f,
        animationSpec = tween(durationMillis = 160),
        label = "cancelGlow",
    )
    val backgroundBrush = remember(colors.todayButtonBackground, glow) {
        Brush.linearGradient(
            colors = listOf(
                colors.todayButtonBackground.copy(alpha = 0.28f + glow * 0.3f),
                colors.todayButtonBackground.copy(alpha = 0.15f + glow * 0.25f),
            ),
        )
    }
    Surface(
        modifier = modifier,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.todayOutline.copy(alpha = borderAlpha)),
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush, shape)
                .padding(vertical = 14.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = colors.cancelButtonContent,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun RowScope.ConfirmActionButton(
    text: String,
    enabled: Boolean,
    colors: DatePickerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.weight(1.2f),
    shape: Shape = RoundedCornerShape(20.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetElevation = when {
        !enabled -> 0.dp
        isPressed -> 18.dp
        else -> 32.dp
    }
    val elevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = tween(durationMillis = 180),
        label = "confirmElevation",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (enabled) 0.85f else 0.4f,
        animationSpec = tween(durationMillis = 200),
        label = "confirmBorder",
    )
    val glow by animateFloatAsState(
        targetValue = if (enabled) {
            if (isPressed) 0.85f else 1f
        } else {
            0.35f
        },
        animationSpec = tween(durationMillis = 200),
        label = "confirmGlow",
    )
    val activeBrush = remember(colors.confirmButtonBackground, colors.brandTeal, glow) {
        Brush.linearGradient(
            colors = listOf(
                colors.confirmButtonBackground.copy(alpha = 0.72f + 0.23f * glow),
                colors.brandTeal.copy(alpha = 0.68f + 0.2f * glow),
            ),
        )
    }
    val disabledBrush = remember(colors.confirmButtonBackground, colors.brandTeal) {
        Brush.linearGradient(
            colors = listOf(
                colors.confirmButtonBackground.copy(alpha = 0.35f),
                colors.brandTeal.copy(alpha = 0.35f),
            ),
        )
    }
    Surface(
        modifier = modifier,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.todayOutline.copy(alpha = borderAlpha)),
        enabled = enabled,
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) activeBrush else disabledBrush, shape)
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = if (enabled) {
                    colors.confirmButtonContent
                } else {
                    colors.confirmButtonContent.copy(alpha = 0.6f)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}