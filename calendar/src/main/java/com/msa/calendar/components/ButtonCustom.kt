package com.msa.calendar.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SnackbarDefaults.contentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.boundingRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ButtonCustom(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(36),
    lightColor: Color,
    darkColor: Color = lightColor.copy(
        red = (lightColor.red + 0.125f).coerceAtMost(1.0f),
        green = (lightColor.green + 0.125f).coerceAtMost(1.0f),
        blue = (lightColor.blue + 0.125f).coerceAtMost(1.0f),
    ),
    onClick: () -> Unit,
    borderWidthPercent: Int = 12,
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = 8.dp,
        pressedElevation = 8.dp,
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    check(borderWidthPercent in 0..100) { "The border width percent should be in the range of [0, 100]" }

    Surface(
        modifier=Modifier
            .clickable {
                onClick
            },
        shape = shape,
        color= lightColor,
        contentColor = contentColor.copy(alpha = 1f),
        shadowElevation =8.dp,
        tonalElevation = 8.dp,
    ) {
        CompositionLocalProvider(
            LocalContentColor
                    provides MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.labelLarge
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = ButtonDefaults.MinWidth
                        )
                        .background(
                            brush = Brush.linearGradient(
                                0.0f to lightColor,
                                1.0f to darkColor
                            )
                        )
                        .drawWithCache {
                            val paint = Paint()
                                .asFrameworkPaint()
                                .apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = size.minDimension * (12 / 100.0f)
                                    shader = LinearGradientShader(
                                        from = Offset.Zero,
                                        to = Offset(x = size.width, y = size.height),
                                        colors = listOf(darkColor, lightColor)
                                    )
                                    maskFilter =
                                        BlurMaskFilter(
                                            strokeWidth / 2,
                                            BlurMaskFilter.Blur.NORMAL
                                        )
                                }

                            onDrawBehind {
                                drawIntoCanvas {
                                    when (val outline =
                                        shape.createOutline(size, layoutDirection, this)) {
                                        is Outline.Rectangle -> {
                                            val rect = outline.rect
                                            it.nativeCanvas.drawRect(
                                                rect.toAndroidRect(),
                                                paint
                                            )
                                        }

                                        is Outline.Rounded -> {
                                            val roundRect = outline.roundRect
                                            it.nativeCanvas.drawRoundRect(
                                                roundRect.boundingRect.toAndroidRectF(),
                                                roundRect.topLeftCornerRadius.x,
                                                roundRect.topLeftCornerRadius.y,
                                                paint
                                            )
                                        }

                                        is Outline.Generic -> {
                                            val path = outline.path
                                            it.nativeCanvas.drawPath(
                                                path.asAndroidPath(),
                                                paint
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }

        }

    }

}

@Composable
@Preview
fun ButtonCustomPreview() {
  MaterialTheme {
    ButtonCustom(
        modifier = Modifier.padding(10.dp),
        lightColor = Color.Gray,
        onClick = { },
    ){
       Text(text = "hs", color = Color.Blue)
    }

  }
}