package com.msa.persiancalendar.showcase

/** Language choices exposed by the showcase application. */
enum class LocaleOption {
    System,
    Persian,
    English;

    internal fun persistedValue(): String = name

    companion object {
        internal fun fromPersistedValue(value: String?): LocaleOption =
            entries.firstOrNull { it.name == value } ?: System
    }
}
