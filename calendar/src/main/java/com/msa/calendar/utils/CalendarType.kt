package com.msa.calendar.utils

sealed class PickerType{
    object Year: PickerType()
    object Month: PickerType()
    object Day: PickerType()
}
