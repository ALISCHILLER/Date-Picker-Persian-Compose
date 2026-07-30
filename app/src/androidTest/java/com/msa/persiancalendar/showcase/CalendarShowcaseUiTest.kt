package com.msa.persiancalendar.showcase

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.msa.calendar.ui.theme.PersianCalendarTheme
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.SoleimaniDate
import org.junit.Rule
import org.junit.Test

class CalendarShowcaseUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primarySingleDateFlowIsVisibleAndOpensDialog() {
        composeRule.setContent {
            PersianCalendarTheme(dynamicColor = false) {
                val state = rememberCalendarShowcaseState(
                    todayProvider = { SoleimaniDate(1404, 5, 7) },
                    localeResolver = { CalendarLocaleConfiguration.english() },
                    initialLocaleOption = LocaleOption.English,
                )
                val uiState = rememberCalendarShowcaseUiState(state)
                CalendarShowcaseScreen(state = state, uiState = uiState)
            }
        }

        composeRule.onNodeWithText("Single date picker").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Select date").assertIsDisplayed()
        composeRule.onNodeWithText("Confirm").assertIsDisplayed()
    }

    @Test
    fun languageChoicesAreVisible() {
        composeRule.setContent {
            PersianCalendarTheme(dynamicColor = false) {
                val state = rememberCalendarShowcaseState(
                    todayProvider = { SoleimaniDate(1404, 5, 7) },
                    localeResolver = { CalendarLocaleConfiguration.english() },
                    initialLocaleOption = LocaleOption.English,
                )
                val uiState = rememberCalendarShowcaseUiState(state)
                CalendarShowcaseScreen(state = state, uiState = uiState)
            }
        }

        composeRule.onNodeWithText("System default").assertIsDisplayed()
        composeRule.onNodeWithText("Persian").assertIsDisplayed()
        composeRule.onNodeWithText("English").assertIsDisplayed()
    }
}
