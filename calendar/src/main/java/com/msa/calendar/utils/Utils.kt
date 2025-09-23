package com.msa.calendar.utils


val persianWeekDays = listOf(
    "شنبه",
    "یکشنبه",
    "دوشنبه",
    "سه شنبه",
    "چهارشنبه",
    "پنجشنبه",
    "جمعه",
)

val monthsList = listOf(
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


fun getWeekDays(monthName: String, year: String): List<String> {
    val monthIndex = monthsList.indexOf(monthName)
    if (monthIndex == -1) {
        return emptyList()
    }
    val yearInt = year.toIntSafely() ?: PersionCalendar().getYear()
    val firstDayOfMonth = PersionCalendar(yearInt, monthIndex + 1, 1)
    val weekDay = firstDayOfMonth.dayOfWeek()
    val daysInMonth = when {
        monthIndex < 6 -> 31
        monthIndex < 11 -> 30
        else -> if (PersionCalendar(yearInt, 12, 1).isLeap()) 30 else 29
    }

    return buildList {
        if (weekDay != 7) {
            repeat(weekDay) { add(" ") }
        }
        for (day in 1..daysInMonth) {
            add(day.toPersianNumber())
        }
    }
}

fun addLeadingZero(number: Int): String {
    return number.toString().padStart(2, '0')
}

fun String.toEnglishDigits(): String {
    if (isEmpty()) return this
    val builder = StringBuilder(length)
    for (character in this) {
        val mappedChar = when (character) {
            '۰', '٠' -> '0'
            '۱', '١' -> '1'
            '۲', '٢' -> '2'
            '۳', '٣' -> '3'
            '۴', '٤' -> '4'
            '۵', '٥' -> '5'
            '۶', '٦' -> '6'
            '۷', '٧' -> '7'
            '۸', '٨' -> '8'
            '۹', '٩' -> '9'
            else -> character
        }
        builder.append(mappedChar)
    }
    return builder.toString()
}

fun String.toIntSafely(): Int? {
    val normalized = toEnglishDigits().trim()
    if (normalized.isEmpty()) return null
    return normalized.toIntOrNull()
}
