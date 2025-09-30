package com.msa.calendar.utils


import java.time.DayOfWeek


data class MonthDayCell(
    val dayOfMonth: Int?,
    val date: SoleimaniDate?,
    val dayOfWeek: DayOfWeek,
    val weekdayIndex: Int,
)

fun buildMonthCells(month: Int, year: Int, startDay: DayOfWeek): List<MonthDayCell> {
    if (month !in 1..12) {
        return emptyList()
    }
    val firstDay = PersianCalendarEngine.dayOfWeek(year, month, 1)
    val daysInMonth = PersianCalendarEngine.monthLength(year, month)
    val offset = firstDay.indexRelativeTo(startDay)

    return buildList {
        repeat(offset) { index ->
            val dayOfWeek = dayOfWeekFromOffset(startDay, index)
            add(
                MonthDayCell(
                    dayOfMonth = null,
                    date = null,
                    dayOfWeek = dayOfWeek,
                    weekdayIndex = dayOfWeek.indexRelativeTo(startDay),
                )
            )
        }
        for (day in 1..daysInMonth) {
            val cellOffset = offset + day - 1
            val dayOfWeek = dayOfWeekFromOffset(startDay, cellOffset)
            add(
                MonthDayCell(
                    dayOfMonth = day,
                    date = SoleimaniDate(year, month, day),
                    dayOfWeek = dayOfWeek,
                    weekdayIndex = dayOfWeek.indexRelativeTo(startDay),
                )
            )
        }
    }
}



private fun dayOfWeekFromOffset(startDay: DayOfWeek, offset: Int): DayOfWeek {
    val normalized = ((startDay.value - 1 + offset) % 7 + 7) % 7
    return DayOfWeek.of(normalized + 1)
}

fun DayOfWeek.indexRelativeTo(startDay: DayOfWeek): Int {
    return ((value - startDay.value) % 7 + 7) % 7
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
