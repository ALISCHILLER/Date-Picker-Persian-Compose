package com.msa.calendar.utils

import java.time.DayOfWeek

/**
 * Provides localized month and weekday labels for the calendar module.
 */
object CalendarTextRepository {

    private val persianMonthFallback = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند",
    )

    private val persianMonthLatinFallback = listOf(
        "Farvardin",
        "Ordibehesht",
        "Khordad",
        "Tir",
        "Mordad",
        "Shahrivar",
        "Mehr",
        "Aban",
        "Azar",
        "Dey",
        "Bahman",
        "Esfand",
    )

    private val gregorianMonthFaFallback = listOf(
        "ژانویه",
        "فوریه",
        "مارس",
        "آوریل",
        "مه",
        "ژوئن",
        "ژوئیه",
        "اوت",
        "سپتامبر",
        "اکتبر",
        "نوامبر",
        "دسامبر",
    )

    private val gregorianMonthEnFallback = listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

    private val persianWeekdayFullFallback = listOf(
        "شنبه",
        "یکشنبه",
        "دوشنبه",
        "سه‌شنبه",
        "چهارشنبه",
        "پنجشنبه",
        "جمعه",
    )

    private val persianWeekdayShortFallback = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    private val gregorianWeekdayShortFaFallback = listOf("د", "س", "چ", "پ", "ج", "ش", "ی")
    private val latinWeekdayShortFallback = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    private fun providerOrDefault(provider: CalendarResourceProvider?): CalendarResourceProvider =
        provider ?: CalendarResourceResolver.provider()

    private fun CalendarResourceProvider.safeStringArray(
        id: Int,
        fallback: List<String>,
        expectedSize: Int,
    ): List<String> {
        val resolved = stringArray(id, fallback)
        return if (resolved.size == expectedSize) resolved else fallback
    }

    fun persianMonthNames(
        provider: CalendarResourceProvider? = null,
    ): List<String> = providerOrDefault(provider).safeStringArray(
        id = com.msa.calendar.R.array.persian_months,
        fallback = persianMonthFallback,
        expectedSize = 12,
    )

    fun persianMonthLatinNames(
        provider: CalendarResourceProvider? = null,
    ): List<String> = providerOrDefault(provider).safeStringArray(
        id = com.msa.calendar.R.array.persian_months_latin,
        fallback = persianMonthLatinFallback,
        expectedSize = 12,
    )

    fun gregorianMonthNamesFa(
        provider: CalendarResourceProvider? = null,
    ): List<String> = providerOrDefault(provider).safeStringArray(
        id = com.msa.calendar.R.array.gregorian_months_fa,
        fallback = gregorianMonthFaFallback,
        expectedSize = 12,
    )

    fun gregorianMonthNamesEn(
        provider: CalendarResourceProvider? = null,
    ): List<String> = providerOrDefault(provider).safeStringArray(
        id = com.msa.calendar.R.array.gregorian_months_en,
        fallback = gregorianMonthEnFallback,
        expectedSize = 12,
    )

    fun persianWeekdayFull(
        day: DayOfWeek,
        provider: CalendarResourceProvider? = null,
    ): String {
        val index = day.indexRelativeTo(DayOfWeek.SATURDAY)
        return providerOrDefault(provider).safeStringArray(
            id = com.msa.calendar.R.array.persian_weekdays_full,
            fallback = persianWeekdayFullFallback,
            expectedSize = 7,
        )[index]
    }

    fun persianWeekdayShort(
        day: DayOfWeek,
        provider: CalendarResourceProvider? = null,
    ): String {
        val index = day.indexRelativeTo(DayOfWeek.SATURDAY)
        return providerOrDefault(provider).safeStringArray(
            id = com.msa.calendar.R.array.persian_weekdays_short,
            fallback = persianWeekdayShortFallback,
            expectedSize = 7,
        )[index]
    }

    fun gregorianWeekdayShortFa(
        day: DayOfWeek,
        provider: CalendarResourceProvider? = null,
    ): String {
        val index = day.indexRelativeTo(DayOfWeek.MONDAY)
        return providerOrDefault(provider).safeStringArray(
            id = com.msa.calendar.R.array.gregorian_weekdays_short_fa,
            fallback = gregorianWeekdayShortFaFallback,
            expectedSize = 7,
        )[index]
    }

    fun latinWeekdayShort(
        day: DayOfWeek,
        provider: CalendarResourceProvider? = null,
    ): String {
        val index = day.indexRelativeTo(DayOfWeek.MONDAY)
        return providerOrDefault(provider).safeStringArray(
            id = com.msa.calendar.R.array.latin_weekdays_short,
            fallback = latinWeekdayShortFallback,
            expectedSize = 7,
        )[index]
    }

    fun persianMonthName(
        month: Int,
        provider: CalendarResourceProvider? = null,
    ): String {
        require(month in 1..12) { "Month must be between 1 and 12" }
        return persianMonthNames(provider)[month - 1]
    }
}
