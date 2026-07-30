package com.msa.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.msa.calendar.ui.DatePickerConfig
import com.msa.calendar.ui.DatePickerConstraints
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.DigitMode
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Rule
import org.junit.Test

class CalendarDialogComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dialogExposesPaneTitleForAccessibilityServices() {
        val strings = DatePickerStrings.localized().copy(title = "Select date")

        composeRule.setContent {
            MaterialTheme {
                CalendarScreen(
                    onDismiss = {},
                    onConfirm = {},
                    initialDate = SoleimaniDate(1404, 1, 1),
                    config = DatePickerConfig(strings = strings, yearRange = 1404..1404),
                )
            }
        }

        composeRule.onNode(
            hasTestTag(DatePickerTestTags.SingleDialog) and
                SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Select date"),
            useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    @Test
    fun navigationButtonsRespectConfiguredYearBoundary() {
        val strings = DatePickerStrings.localized().copy(
            previousMonth = "Previous month",
            nextMonth = "Next month",
            confirm = "Confirm",
        )

        composeRule.setContent {
            MaterialTheme {
                CalendarScreen(
                    onDismiss = {},
                    onConfirm = {},
                    initialDate = SoleimaniDate(1400, 1, 1),
                    config = DatePickerConfig(
                        strings = strings,
                        yearRange = 1400..1400,
                    ),
                )
            }
        }

        composeRule.onNodeWithTag(DatePickerTestTags.SingleDialog).assertIsDisplayed()
        composeRule.onNodeWithTag(DatePickerTestTags.MonthGrid).assertIsDisplayed()
        composeRule.onNodeWithTag(DatePickerTestTags.ActionBar).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Previous month").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Next month").assertIsEnabled()
        composeRule.onNodeWithText("Confirm").assertIsDisplayed()
    }

    @Test
    fun rangeCannotCrossDisabledInteriorDate() {
        val strings = DatePickerStrings.localized().copy(
            previousMonth = "Previous month",
            nextMonth = "Next month",
            disabledState = "Disabled",
        )

        composeRule.setContent {
            MaterialTheme {
                RangeCalendarScreen(
                    onDismiss = {},
                    setDate = {},
                    initialStartDate = SoleimaniDate(1404, 1, 1),
                    config = DatePickerConfig(
                        strings = strings,
                        digitMode = DigitMode.Latin,
                        yearRange = 1404..1404,
                        constraints = DatePickerConstraints(
                            disabledDates = setOf(SoleimaniDate(1404, 1, 2)),
                        ),
                    ),
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("1404/01/03, disabled")
            .assertIsNotEnabled()
    }

    @Test
    fun rangeHeaderKeepsPersianNumericDatesVisibleInRtl() {
        val strings = DatePickerStrings.localized().copy(
            title = "انتخاب بازه",
            rangeStartLabel = "شروع",
            rangeEndLabel = "پایان",
            rangeSeparator = "تا",
        )

        composeRule.setContent {
            MaterialTheme {
                RangeCalendarScreen(
                    onDismiss = {},
                    setDate = {},
                    initialStartDate = SoleimaniDate(1404, 1, 1),
                    initialEndDate = SoleimaniDate(1404, 1, 21),
                    config = DatePickerConfig(
                        strings = strings,
                        digitMode = DigitMode.Persian,
                        yearRange = 1404..1404,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("۱۴۰۴/۰۱/۰۱").assertIsDisplayed()
        composeRule.onNodeWithText("۱۴۰۴/۰۱/۲۱").assertIsDisplayed()
        composeRule.onNodeWithText("شروع").assertIsDisplayed()
        composeRule.onNodeWithText("پایان").assertIsDisplayed()
    }

    @Test
    fun rangeHeaderKeepsBothNumericDatesVisible() {
        val strings = DatePickerStrings.localized().copy(
            title = "Select range",
            rangeStartLabel = "Start date",
            rangeEndLabel = "End date",
            rangeSeparator = "to",
        )

        composeRule.setContent {
            MaterialTheme {
                RangeCalendarScreen(
                    onDismiss = {},
                    setDate = {},
                    initialStartDate = SoleimaniDate(1404, 1, 1),
                    initialEndDate = SoleimaniDate(1404, 1, 21),
                    config = DatePickerConfig(
                        strings = strings,
                        digitMode = DigitMode.Latin,
                        yearRange = 1404..1404,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("1404/01/01").assertIsDisplayed()
        composeRule.onNodeWithText("1404/01/21").assertIsDisplayed()
        composeRule.onNodeWithText("Start date").assertIsDisplayed()
        composeRule.onNodeWithText("End date").assertIsDisplayed()
    }

    @Test
    fun selectedSingleDateShowsGregorianEquivalent() {
        val strings = DatePickerStrings.localized().copy(
            title = "انتخاب تاریخ",
            gregorianCalendarLabel = "میلادی",
        )

        composeRule.setContent {
            MaterialTheme {
                CalendarScreen(
                    onDismiss = {},
                    onConfirm = {},
                    initialDate = SoleimaniDate(1404, 1, 1),
                    config = DatePickerConfig(
                        strings = strings,
                        digitMode = DigitMode.Persian,
                        yearRange = 1404..1404,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("میلادی: ۲۱ مارس ۲۰۲۵").assertIsDisplayed()
    }

    @Test
    fun selectedRangeShowsExactGregorianDates() {
        val strings = DatePickerStrings.localized().copy(
            title = "انتخاب بازه",
            rangeStartLabel = "شروع",
            rangeEndLabel = "پایان",
            rangeSeparator = "تا",
            gregorianCalendarLabel = "میلادی",
        )

        composeRule.setContent {
            MaterialTheme {
                RangeCalendarScreen(
                    onDismiss = {},
                    setDate = {},
                    initialStartDate = SoleimaniDate(1404, 1, 1),
                    initialEndDate = SoleimaniDate(1404, 1, 21),
                    config = DatePickerConfig(
                        strings = strings,
                        digitMode = DigitMode.Persian,
                        yearRange = 1404..1404,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("میلادی: مارس – آوریل ۲۰۲۵").assertIsDisplayed()
        composeRule.onNodeWithText("۲۱ مارس ۲۰۲۵").assertIsDisplayed()
        composeRule.onNodeWithText("۱۰ آوریل ۲۰۲۵").assertIsDisplayed()
    }

    @Test
    fun veryLargeFontUsesCondensedRangeHeaderWithoutHidingCalendarActions() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 1.6f),
            ) {
                MaterialTheme {
                    RangeCalendarScreen(
                        onDismiss = {},
                        setDate = {},
                        initialStartDate = SoleimaniDate(1404, 1, 1),
                        initialEndDate = SoleimaniDate(1404, 1, 21),
                        config = DatePickerConfig(
                            digitMode = DigitMode.Latin,
                            yearRange = 1404..1404,
                        ),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(DatePickerTestTags.RangeHeader).assertIsDisplayed()
        composeRule.onNodeWithTag(DatePickerTestTags.MonthGrid).assertIsDisplayed()
        composeRule.onNodeWithTag(DatePickerTestTags.ActionBar).assertIsDisplayed()
        composeRule.onNodeWithText("1404/01/01").assertIsDisplayed()
        composeRule.onNodeWithText("1404/01/21").assertIsDisplayed()
    }

}
