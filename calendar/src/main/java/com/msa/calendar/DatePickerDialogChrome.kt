package com.msa.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.msa.calendar.ui.DatePickerColors

/** Shared, cached dialog chrome used by both single-date and range pickers. */
internal fun Modifier.datePickerDialogChrome(
    shape: Shape,
    colors: DatePickerColors,
    title: String,
    primaryAccentAlpha: Float,
    secondaryAccentAlpha: Float,
    primaryCenterYFraction: Float,
): Modifier = this
    .fillMaxWidth()
    .semantics { contentDescription = title }
    .drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val outlinePath = when (outline) {
            is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
            is Outline.Generic -> outline.path
            else -> null
        }
        val primaryRadius = size.width * 0.9f
        val primary = Brush.radialGradient(
            colors = listOf(
                colors.brandViolet.copy(alpha = primaryAccentAlpha),
                Color.Transparent,
            ),
            center = Offset(0f, size.height * primaryCenterYFraction),
            radius = primaryRadius,
        )
        val secondary = Brush.radialGradient(
            colors = listOf(
                colors.brandTeal.copy(alpha = secondaryAccentAlpha),
                Color.Transparent,
            ),
            center = Offset(size.width, size.height),
            radius = primaryRadius * 0.82f,
        )

        onDrawBehind {
            if (outlinePath != null) {
                drawPath(outlinePath, colors.containerColor)
                drawPath(outlinePath, primary)
                drawPath(outlinePath, secondary)
            } else {
                drawRect(colors.containerColor)
                drawRect(primary)
                drawRect(secondary)
            }
        }
    }
