package com.msa.calendar.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.msa.calendar.ui.DatePickerColors

/** Lightweight, ripple-driven choice tile used by month and year pickers. */
@Composable
internal fun PickerChoiceCard(
    selected: Boolean,
    highlighted: Boolean,
    colors: DatePickerColors,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val selectedBrush = remember(colors.brandViolet, colors.brandTeal) {
        Brush.linearGradient(colors = listOf(colors.brandViolet, colors.brandTeal))
    }
    val background = when {
        selected -> Color.Transparent
        highlighted -> colors.currentChoiceColor
        else -> colors.surfaceVariantColor.copy(alpha = 0.76f)
    }
    val border = when {
        selected -> BorderStroke(1.dp, colors.selectionContentColor.copy(alpha = 0.28f))
        highlighted -> BorderStroke(1.dp, colors.todayOutline.copy(alpha = 0.48f))
        else -> BorderStroke(1.dp, colors.outlineColor)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            role = Role.Button
            this.contentDescription = contentDescription
            if (selected) this.selected = true
        },
        shape = shape,
        color = background,
        contentColor = colors.dayTextColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
    ) {
        Box(
            modifier = Modifier
                .then(if (selected) Modifier.background(selectedBrush, shape) else Modifier)
                .defaultMinSize(minHeight = 54.dp)
                .fillMaxWidth()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
