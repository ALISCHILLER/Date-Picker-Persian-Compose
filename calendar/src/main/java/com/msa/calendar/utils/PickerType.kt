package com.msa.calendar.utils

internal sealed interface PickerType {
    data object Year : PickerType
    data object Month : PickerType
    data object Day : PickerType
}
