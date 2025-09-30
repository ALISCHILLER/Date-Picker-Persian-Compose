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

    fun persianMonthNames(): List<String> =
        CalendarResourceResolver.stringArray(
            com.msa.calendar.R.array.persian_months,
            persianMonthFallback,
        )

    fun persianMonthLatinNames(): List<String> =
        CalendarResourceResolver.stringArray(
            com.msa.calendar.R.array.persian_months_latin,
            persianMonthLatinFallback,
        )

    fun gregorianMonthNamesFa(): List<String> =
        CalendarResourceResolver.stringArray(
            com.msa.calendar.R.array.gregorian_months_fa,
            gregorianMonthFaFallback,
        )

    fun gregorianMonthNamesEn(): List<String> =
        CalendarResourceResolver.stringArray(
            com.msa.calendar.R.array.gregorian_months_en,
            gregorianMonthEnFallback,
        )

    fun persianWeekdayFull(day: DayOfWeek): String {
        val index = day.indexRelativeTo(DayOfWeek.SATURDAY)
        return CalendarResourceResolver
            .stringArray(com.msa.calendar.R.array.persian_weekdays_full, persianWeekdayFullFallback)[index]
    }

    fun persianWeekdayShort(day: DayOfWeek): String {
        val index = day.indexRelativeTo(DayOfWeek.SATURDAY)
        return CalendarResourceResolver
            .stringArray(com.msa.calendar.R.array.persian_weekdays_short, persianWeekdayShortFallback)[index]
    }

    fun gregorianWeekdayShortFa(day: DayOfWeek): String {
        val index = day.indexRelativeTo(DayOfWeek.MONDAY)
        return CalendarResourceResolver
            .stringArray(com.msa.calendar.R.array.gregorian_weekdays_short_fa, gregorianWeekdayShortFaFallback)[index]
    }

    fun latinWeekdayShort(day: DayOfWeek): String {
        val index = day.indexRelativeTo(DayOfWeek.MONDAY)
        return CalendarResourceResolver
            .stringArray(com.msa.calendar.R.array.latin_weekdays_short, latinWeekdayShortFallback)[index]
    }

    fun persianMonthName(month: Int): String {
        require(month in 1..12) { "Month must be between 1 and 12" }
        return persianMonthNames()[month - 1]
    }
}