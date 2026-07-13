package com.msa.calendar

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.msa.calendar.ui.DatePickerStrings
import com.msa.calendar.ui.MonthFormatter
import com.msa.calendar.utils.AndroidCalendarResourceProvider
import com.msa.calendar.utils.CalendarLocaleConfiguration
import com.msa.calendar.utils.CalendarLocalization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class CalendarResourcesInstrumentedTest {


    @Test
    fun androidProviderWithoutExplicitConfigurationUsesThePassedContextLocale() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale("fa"))
        }
        val persianContext = context.createConfigurationContext(configuration)
        val provider = AndroidCalendarResourceProvider(persianContext)

        val months = MonthFormatter.persian(provider).labels(com.msa.calendar.ui.DigitMode.Persian)

        assertEquals("فروردین", months.first())
        assertEquals("اسفند", months.last())
    }


    @Test
    fun localizedContextCanResolveExplicitEnglishAndPersianResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val english = CalendarLocalization.localizedContext(context, CalendarLocaleConfiguration.english())
        val persian = CalendarLocalization.localizedContext(context, CalendarLocaleConfiguration.persian())

        assertEquals("Confirm", english.getString(R.string.calendar_picker_confirm))
        assertEquals("تأیید", persian.getString(R.string.calendar_picker_confirm))
    }

    @Test
    fun localizedProviderResolvesPersianStringsAndMonthsFromAndroidResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val provider = AndroidCalendarResourceProvider(context, CalendarLocaleConfiguration.persian())

        val strings = DatePickerStrings.localized(provider)
        val months = MonthFormatter.persian(provider).labels(com.msa.calendar.ui.DigitMode.Persian)

        assertTrue(strings.confirm.isNotBlank())
        assertTrue(strings.cancel.isNotBlank())
        assertEquals(12, months.size)
        assertEquals("فروردین", months.first())
        assertEquals("اسفند", months.last())
    }
}
