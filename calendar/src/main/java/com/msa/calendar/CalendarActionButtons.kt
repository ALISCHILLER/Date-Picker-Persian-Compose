package com.msa.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msa.calendar.ui.DatePickerColors

/**
 * Responsive action area shared by both picker types.
 *
 * Actions remain on one line on ordinary phones and stack at very large font scales or genuinely
 * narrow widths. This avoids shrinking Persian labels while keeping the common path compact.
 */
@Composable
internal fun DatePickerActionBar(
    cancelText: String,
    confirmText: String,
    confirmEnabled: Boolean,
    colors: DatePickerColors,
    compact: Boolean,
    verticalPadding: androidx.compose.ui.unit.Dp,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(DatePickerTestTags.ActionBar)
            .padding(
                horizontal = if (compact) 14.dp else 20.dp,
                vertical = verticalPadding,
            ),
    ) {
        val stackActions = DatePickerDialogMetrics.shouldStackActions(
            availableWidthDp = maxWidth.value,
            fontScale = fontScale,
        )
        if (stackActions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ConfirmActionButtonContent(
                    text = confirmText,
                    enabled = confirmEnabled,
                    colors = colors,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                )
                CancelActionButtonContent(
                    text = cancelText,
                    colors = colors,
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CancelActionButtonContent(
                    text = cancelText,
                    colors = colors,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                )
                ConfirmActionButtonContent(
                    text = confirmText,
                    enabled = confirmEnabled,
                    colors = colors,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1.18f),
                )
            }
        }
    }
}

/** Kept for source compatibility with existing call sites. */
@Composable
fun RowScope.CancelActionButton(
    text: String,
    colors: DatePickerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.weight(1f),
    shape: Shape = RoundedCornerShape(17.dp),
) {
    CancelActionButtonContent(
        text = text,
        colors = colors,
        onClick = onClick,
        modifier = modifier,
        shape = shape,
    )
}

/** Kept for source compatibility with existing call sites. */
@Composable
fun RowScope.ConfirmActionButton(
    text: String,
    enabled: Boolean,
    colors: DatePickerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.weight(1.18f),
    shape: Shape = RoundedCornerShape(17.dp),
) {
    ConfirmActionButtonContent(
        text = text,
        enabled = enabled,
        colors = colors,
        onClick = onClick,
        modifier = modifier,
        shape = shape,
    )
}

@Composable
private fun CancelActionButtonContent(
    text: String,
    colors: DatePickerColors,
    onClick: () -> Unit,
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(17.dp),
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            role = Role.Button
            contentDescription = text
        },
        shape = shape,
        color = colors.surfaceVariantColor.copy(alpha = 0.82f),
        contentColor = colors.cancelButtonContent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, colors.outlineColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = colors.cancelButtonContent.copy(alpha = 0.82f),
                modifier = Modifier.padding(end = 7.dp),
            )
            Text(
                text = text,
                color = colors.cancelButtonContent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ConfirmActionButtonContent(
    text: String,
    enabled: Boolean,
    colors: DatePickerColors,
    onClick: () -> Unit,
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(17.dp),
) {
    val activeBrush = remember(colors.confirmButtonBackground, colors.brandTeal) {
        Brush.horizontalGradient(
            colors = listOf(
                colors.confirmButtonBackground,
                colors.brandTeal,
            ),
        )
    }
    val disabledBrush = remember(colors.surfaceVariantColor) {
        Brush.horizontalGradient(
            colors = listOf(colors.surfaceVariantColor, colors.surfaceVariantColor),
        )
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.semantics {
            role = Role.Button
            contentDescription = text
            if (!enabled) disabled()
        },
        shape = shape,
        color = Color.Transparent,
        contentColor = colors.confirmButtonContent,
        tonalElevation = 0.dp,
        shadowElevation = if (enabled) 3.dp else 0.dp,
        border = BorderStroke(
            1.dp,
            if (enabled) colors.brandTeal.copy(alpha = 0.48f) else colors.outlineColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (enabled) activeBrush else disabledBrush, shape)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = if (enabled) colors.confirmButtonContent else colors.disabledDayTextColor,
                modifier = Modifier.padding(end = 7.dp),
            )
            Text(
                text = text,
                color = if (enabled) colors.confirmButtonContent else colors.disabledDayTextColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
