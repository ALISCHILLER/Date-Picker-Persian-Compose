package com.msa.persioncalendar.model

import com.msa.persioncalendar.utils.PersianMonth

internal data class CalendarMonthData(
    val selected: PersianMonth,
    val disabled: List<PersianMonth>,
    val thisMonth: PersianMonth,
)
