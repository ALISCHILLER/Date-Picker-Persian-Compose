package com.msa.calendar.utils


import java.time.DayOfWeek

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
data class MonthDayCell(
    val dayOfMonth: Int?,
    val date: SoleimaniDate?,
    val dayOfWeek: DayOfWeek,
)

fun buildMonthCells(monthName: String, year: String, startDay: DayOfWeek): List<MonthDayCell> {
    val monthIndex = monthsList.indexOf(monthName)
    if (monthIndex == -1) {
        return emptyList()
    }
    val yearInt = year.toIntSafely() ?: PersionCalendar().getYear()
    val firstDayOfMonth = PersionCalendar(yearInt, monthIndex + 1, 1)
    val firstDay = firstDayOfMonth.dayOfWeek().toDayOfWeek()
    val daysInMonth = when {
        monthIndex < 6 -> 31
        monthIndex < 11 -> 30
        else -> if (PersionCalendar(yearInt, 12, 1).isLeap()) 30 else 29
    }
    val offset = ((firstDay.value - startDay.value + 7) % 7)

    return buildList {
        repeat(offset) { index ->
            add(
                MonthDayCell(
                    dayOfMonth = null,
                    date = null,
                    dayOfWeek = dayOfWeekFromOffset(startDay, index),
                )
            )
        }
        for (day in 1..daysInMonth) {
            val cellOffset = offset + day - 1
            add(
                MonthDayCell(
                    dayOfMonth = day,
                    date = SoleimaniDate(yearInt, monthIndex + 1, day),
                    dayOfWeek = dayOfWeekFromOffset(startDay, cellOffset),
                )
            )
        }
    }
}


internal fun Int.toDayOfWeek(): DayOfWeek {
    val normalized = ((this + 5) % 7 + 7) % 7
    return DayOfWeek.of(normalized + 1)
}

private fun dayOfWeekFromOffset(startDay: DayOfWeek, offset: Int): DayOfWeek {
    val normalized = ((startDay.value - 1 + offset) % 7 + 7) % 7
    return DayOfWeek.of(normalized + 1)
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
