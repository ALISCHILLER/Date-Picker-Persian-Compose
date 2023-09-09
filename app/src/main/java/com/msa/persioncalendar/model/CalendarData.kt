package com.msa.persioncalendar.model

import java.time.LocalDate

internal data class CalendarData(
    val offsetStart: Int,
    val weekCameraDate: LocalDate,
    val cameraDate: LocalDate,
    val days: Int,
)
