package com.msa.calendar.utils

import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.GregorianCalendar

/**
 * Correctly spelled public calendar API.
 *
 * It extends the original [PersionCalendar] implementation so existing binary consumers keep the
 * old class while Kotlin and Java callers can migrate to a correctly named type.
 */
class PersianCalendar : PersionCalendar {
    constructor(zoneId: ZoneId = ZoneId.systemDefault()) : super(zoneId)
    constructor(year: Int, month: Int, day: Int) : super(year, month, day)
    constructor(calendar: GregorianCalendar) : super(calendar)
    constructor(date: LocalDate) : super(date)
    constructor(date: Date) : super(date)
}
