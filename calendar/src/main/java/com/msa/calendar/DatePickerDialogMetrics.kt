package com.msa.calendar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout modes for the range summary.
 *
 * [Inline] keeps both endpoints side by side, [Stacked] preserves all supporting details on narrow
 * portrait screens, and [Condensed] minimizes vertical usage on short/landscape windows.
 */
internal enum class RangeHeaderLayoutMode {
    Inline,
    Stacked,
    Condensed,
}

/**
 * Measurements resolved from the *actual Compose constraints* available to the dialog.
 *
 * This is intentionally not based on LocalConfiguration. Configuration width/height describe the
 * device window, while a dialog can receive less space because of safe drawing insets, split-screen,
 * freeform windows, display cut-outs, the IME, or an embedding host.
 */
@Immutable
internal data class DatePickerDialogLayout(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val maxWidth: Dp,
    val maxHeight: Dp,
    val compactHeader: Boolean,
    val minimalHeader: Boolean,
    val compactCalendar: Boolean,
    val rangeHeaderMode: RangeHeaderLayoutMode,
    val actionVerticalPadding: Dp,
    val dividerTopPadding: Dp,
    val showQuickActions: Boolean,
)

internal object DatePickerDialogMetrics {
    private const val TinyWidthThresholdDp = 320f
    private const val NarrowWidthThresholdDp = 380f
    private const val TabletWidthThresholdDp = 600f
    private const val VeryShortHeightThresholdDp = 500f
    private const val MinimalHeaderHeightThresholdDp = 560f
    private const val ShortHeightThresholdDp = 640f
    private const val LargeFontScaleThreshold = 1.30f
    private const val VeryLargeFontScaleThreshold = 1.55f

    val MinSafeDimension: Dp = 1.dp
    val PhoneMaxWidth: Dp = 480.dp
    val TabletMaxWidth: Dp = 560.dp
    val MaxContainerHeight: Dp = 780.dp

    /** Compatibility overload for unit tests and callers that work with integer dp values. */
    fun resolve(
        screenWidthDp: Int,
        screenHeightDp: Int,
        fontScale: Float,
    ): DatePickerDialogLayout = resolveAvailableSpace(
        availableWidthDp = screenWidthDp.toFloat(),
        availableHeightDp = screenHeightDp.toFloat(),
        fontScale = fontScale,
    )

    /** Resolve layout from the exact constraints visible inside the dialog's safe drawing area. */
    fun resolveAvailableSpace(
        availableWidthDp: Float,
        availableHeightDp: Float,
        fontScale: Float,
    ): DatePickerDialogLayout {
        val safeWidth = availableWidthDp.coerceAtLeast(1f)
        val safeHeight = availableHeightDp.coerceAtLeast(1f)
        val isLandscape = safeWidth > safeHeight
        val isTiny = safeWidth < TinyWidthThresholdDp
        val isNarrow = safeWidth < NarrowWidthThresholdDp
        val isTablet = safeWidth >= TabletWidthThresholdDp
        val isVeryShort = safeHeight < VeryShortHeightThresholdDp
        val isShort = safeHeight < ShortHeightThresholdDp
        val usesLargeText = fontScale >= LargeFontScaleThreshold
        val usesVeryLargeText = fontScale >= VeryLargeFontScaleThreshold

        val horizontalPadding = when {
            isTiny -> 2.dp
            isNarrow -> 4.dp
            isTablet -> 24.dp
            else -> 10.dp
        }
        val verticalPadding = when {
            isVeryShort -> 2.dp
            isLandscape || safeHeight < 580f -> 4.dp
            safeHeight < 720f -> 8.dp
            else -> 14.dp
        }

        val usableWidth = (safeWidth.dp - horizontalPadding * 2)
            .coerceAtLeast(MinSafeDimension)
        val usableHeight = (safeHeight.dp - verticalPadding * 2)
            .coerceAtLeast(MinSafeDimension)
        val widthCap = if (isTablet) TabletMaxWidth else PhoneMaxWidth

        val minimalHeader = safeHeight < MinimalHeaderHeightThresholdDp ||
            (isLandscape && safeHeight < 540f) ||
            usesVeryLargeText
        val compactHeader = isNarrow || isShort || isLandscape || usesLargeText
        val compactCalendar = isNarrow || isShort || isLandscape || usesLargeText

        val rangeHeaderMode = when {
            minimalHeader -> RangeHeaderLayoutMode.Condensed
            isNarrow || usesLargeText -> RangeHeaderLayoutMode.Stacked
            else -> RangeHeaderLayoutMode.Inline
        }

        return DatePickerDialogLayout(
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            maxWidth = usableWidth.coerceAtMost(widthCap),
            maxHeight = usableHeight.coerceAtMost(MaxContainerHeight),
            compactHeader = compactHeader,
            minimalHeader = minimalHeader,
            compactCalendar = compactCalendar,
            rangeHeaderMode = rangeHeaderMode,
            actionVerticalPadding = when {
                isVeryShort -> 6.dp
                compactHeader -> 9.dp
                else -> 14.dp
            },
            dividerTopPadding = if (minimalHeader) 2.dp else if (compactHeader) 4.dp else 7.dp,
            showQuickActions = !minimalHeader,
        )
    }

    fun shouldStackRangeHeader(
        availableWidthDp: Float,
        fontScale: Float,
        forceStacked: Boolean = false,
    ): Boolean = forceStacked ||
        fontScale >= LargeFontScaleThreshold ||
        availableWidthDp < 340f

    fun shouldStackActions(
        availableWidthDp: Float,
        fontScale: Float,
    ): Boolean = availableWidthDp < 306f || fontScale >= 1.42f

    // Kept for source compatibility with existing internal tests and call sites.
    fun verticalPaddingFor(screenHeightDp: Int): Dp = when {
        screenHeightDp < 500 -> 2.dp
        screenHeightDp < 580 -> 4.dp
        screenHeightDp < 720 -> 8.dp
        else -> 14.dp
    }

    fun maxHeightFor(screenHeightDp: Int, verticalPadding: Dp): Dp {
        val available = screenHeightDp.coerceAtLeast(1).dp - verticalPadding * 2
        return available
            .coerceAtLeast(MinSafeDimension)
            .coerceAtMost(MaxContainerHeight)
    }

    fun useCompactLayoutFor(screenHeightDp: Int): Boolean =
        screenHeightDp < ShortHeightThresholdDp

    fun actionVerticalPaddingFor(screenHeightDp: Int): Dp = when {
        screenHeightDp < VeryShortHeightThresholdDp -> 6.dp
        useCompactLayoutFor(screenHeightDp) -> 9.dp
        else -> 14.dp
    }

    fun dividerTopPaddingFor(screenHeightDp: Int): Dp = when {
        screenHeightDp < VeryShortHeightThresholdDp -> 2.dp
        useCompactLayoutFor(screenHeightDp) -> 4.dp
        else -> 7.dp
    }
}
