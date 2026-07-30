package com.msa.calendar

import androidx.compose.runtime.saveable.Saver
import com.msa.calendar.utils.PickerType
import com.msa.calendar.utils.SoleimaniDate

private const val PickerYear = "year"
private const val PickerMonth = "month"
private const val PickerDay = "day"

internal val PickerTypeSaver: Saver<PickerType, String> = Saver(
    save = { value: PickerType ->
        when (value) {
            PickerType.Year -> PickerYear
            PickerType.Month -> PickerMonth
            PickerType.Day -> PickerDay
        }
    },
    restore = { saved: String ->
        when (saved) {
            PickerYear -> PickerType.Year
            PickerMonth -> PickerType.Month
            else -> PickerType.Day
        }
    },
)

/** Saves nullable picker dates without relying on Java serialization or Parcelable. */
internal val NullableSoleimaniDateSaver: Saver<SoleimaniDate?, List<Int>> = Saver(
    save = { date: SoleimaniDate? ->
        date?.let { listOf(it.year, it.month, it.day) } ?: emptyList()
    },
    restore = { saved: List<Int> ->
        if (saved.size != 3) null
        else runCatching { SoleimaniDate(saved[0], saved[1], saved[2]) }.getOrNull()
    },
)
