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