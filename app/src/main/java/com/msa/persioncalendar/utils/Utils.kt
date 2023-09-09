package com.msa.persioncalendar.utils

import com.msa.persioncalendar.model.CalendarMonthData
import java.time.LocalDate



val persianWeekDays = listOf("شنبه","یکشنبه","دوشنبه","سه شنبه",
    "چهارشنبه","پنجشنبه","جمعه", )

val monthsList = listOf("فروردین", "اردیبهشت", "خرداد", "تیر",
    "مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند",)

fun getweekDay(mMonth: String,mDay : String , mYear: String ): MutableList<String> {

    val weekDay = PersionCalendar(mYear.toInt(),
        monthsList.indexOf(mMonth) + 1, 1).dayOfWeek()
    var daysList = mutableListOf<String>()

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
            daysList.add(i.toString())
        }
    }
    return daysList
}