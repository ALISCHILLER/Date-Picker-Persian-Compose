package com.msa.calendar

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatePickerDialogMetricsTest {

    @Test
    fun tinyWindowUsesActualAvailableSpaceWithoutOverflow() {
        val layout = DatePickerDialogMetrics.resolveAvailableSpace(
            availableWidthDp = 300f,
            availableHeightDp = 480f,
            fontScale = 1f,
        )

        assertEquals(2.dp, layout.horizontalPadding)
        assertEquals(296.dp, layout.maxWidth)
        assertEquals(476.dp, layout.maxHeight)
        assertTrue(layout.compactHeader)
        assertTrue(layout.minimalHeader)
        assertTrue(layout.compactCalendar)
        assertEquals(RangeHeaderLayoutMode.Condensed, layout.rangeHeaderMode)
        assertFalse(layout.showQuickActions)
    }

    @Test
    fun common360DpPhoneKeepsReadableCalendarColumns() {
        val layout = DatePickerDialogMetrics.resolveAvailableSpace(
            availableWidthDp = 360f,
            availableHeightDp = 800f,
            fontScale = 1f,
        )

        assertEquals(4.dp, layout.horizontalPadding)
        assertEquals(352.dp, layout.maxWidth)
        assertTrue(layout.compactHeader)
        assertTrue(layout.compactCalendar)
        assertEquals(RangeHeaderLayoutMode.Stacked, layout.rangeHeaderMode)
        assertTrue(layout.maxWidth.value / 7f >= 50f)
    }

    @Test
    fun regularPhoneUsesInlineRangeSummaryAndStableHeight() {
        val layout = DatePickerDialogMetrics.resolveAvailableSpace(
            availableWidthDp = 412f,
            availableHeightDp = 915f,
            fontScale = 1f,
        )

        assertEquals(10.dp, layout.horizontalPadding)
        assertEquals(392.dp, layout.maxWidth)
        assertEquals(780.dp, layout.maxHeight)
        assertFalse(layout.compactHeader)
        assertFalse(layout.minimalHeader)
        assertFalse(layout.compactCalendar)
        assertEquals(RangeHeaderLayoutMode.Inline, layout.rangeHeaderMode)
        assertTrue(layout.showQuickActions)
    }

    @Test
    fun shortLandscapeUsesCondensedHeaderInsteadOfTallStack() {
        val layout = DatePickerDialogMetrics.resolveAvailableSpace(
            availableWidthDp = 720f,
            availableHeightDp = 360f,
            fontScale = 1f,
        )

        assertEquals(24.dp, layout.horizontalPadding)
        assertEquals(560.dp, layout.maxWidth)
        assertEquals(356.dp, layout.maxHeight)
        assertTrue(layout.compactHeader)
        assertTrue(layout.minimalHeader)
        assertTrue(layout.compactCalendar)
        assertEquals(RangeHeaderLayoutMode.Condensed, layout.rangeHeaderMode)
        assertFalse(layout.showQuickActions)
    }

    @Test
    fun splitScreenConstraintsWinOverDeviceConfiguration() {
        val fullWindow = DatePickerDialogMetrics.resolveAvailableSpace(900f, 1200f, 1f)
        val splitWindow = DatePickerDialogMetrics.resolveAvailableSpace(430f, 520f, 1f)

        assertEquals(560.dp, fullWindow.maxWidth)
        assertTrue(splitWindow.maxWidth < fullWindow.maxWidth)
        assertTrue(splitWindow.maxHeight < fullWindow.maxHeight)
        assertEquals(RangeHeaderLayoutMode.Condensed, splitWindow.rangeHeaderMode)
    }

    @Test
    fun largeFontScaleChangesCompositionWithoutShrinkingWidth() {
        val layout = DatePickerDialogMetrics.resolveAvailableSpace(
            availableWidthDp = 412f,
            availableHeightDp = 915f,
            fontScale = 1.6f,
        )

        assertEquals(392.dp, layout.maxWidth)
        assertTrue(layout.minimalHeader)
        assertTrue(layout.compactCalendar)
        assertEquals(RangeHeaderLayoutMode.Condensed, layout.rangeHeaderMode)
        assertFalse(layout.showQuickActions)
    }

    @Test
    fun actionButtonsStackOnlyForGenuinelyNarrowWidthOrVeryLargeText() {
        assertTrue(DatePickerDialogMetrics.shouldStackActions(300f, 1f))
        assertFalse(DatePickerDialogMetrics.shouldStackActions(352f, 1f))
        assertTrue(DatePickerDialogMetrics.shouldStackActions(352f, 1.5f))
    }

    @Test
    fun legacyHeightHelpersRemainBounded() {
        assertEquals(2.dp, DatePickerDialogMetrics.verticalPaddingFor(480))
        assertEquals(4.dp, DatePickerDialogMetrics.verticalPaddingFor(520))
        assertEquals(8.dp, DatePickerDialogMetrics.verticalPaddingFor(700))
        assertEquals(14.dp, DatePickerDialogMetrics.verticalPaddingFor(800))

        val maxHeight = DatePickerDialogMetrics.maxHeightFor(
            screenHeightDp = 0,
            verticalPadding = 14.dp,
        )
        assertTrue(maxHeight > 0.dp)
    }
}
