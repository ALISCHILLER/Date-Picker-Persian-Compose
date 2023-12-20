package com.msa.calendar.utils


val persianWeekDays = listOf("شنبه","یکشنبه","دوشنبه","سه شنبه",
    "چهارشنبه","پنجشنبه","جمعه", )

val monthsList = listOf("فروردین", "اردیبهشت", "خرداد", "تیر",
    "مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند",)


fun getweekDay(mMonth: String, mYear: String): MutableList<String> {

    val weekDay = PersionCalendar(mYear.toInt(),
        monthsList.indexOf(mMonth) + 1, 1).dayOfWeek()
    val daysList = mutableListOf<String>()

    if (weekDay != 7){
        for (i in 1..weekDay){
            daysList.add(" ")
        }
    }

    if (monthsList.indexOf(mMonth) < 6){
        for (i in 1..31){
            daysList.add(i.toPersianNumber())
        }
    } else {
        for (i in 1..30){
            daysList.add(i.toPersianNumber())
        }
    }
    return daysList
}

fun addLeadingZero(number: Int): String {
    return number.toString().padStart(2, '0')
}

fun List<String>.toIntList(): List<Int> {
    return map { it.toInt() }
}
fun List<Int>.compareTo(other: List<Int>): Int {
    for ((index, value) in withIndex()) {
        if (index >= other.size) {
            // This list is longer than the other list
            return 1
        }

        if (value < other[index]) {
            return -1
        } else if (value > other[index]) {
            return 1
        }
    }

    // Both lists are equal up to the length of the shorter list
    return size.compareTo(other.size)
}


fun List<Int>.isBefore(other: List<Int>): Boolean {
    for (i in 0 until minOf(size, other.size)) {
        if (this[i] < other[i]) {
            return true
        } else if (this[i] > other[i]) {
            return false
        }
    }
    return false
}

fun List<Int>.isAfter(other: List<Int>): Boolean {
    for (i in 0 until minOf(size, other.size)) {
        if (this[i] > other[i]) {
            return true
        } else if (this[i] < other[i]) {
            return false
        }
    }
    return false
}
