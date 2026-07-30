package com.msa.persiancalendar.showcase

import android.content.Context

/** Minimal persistence boundary for the showcase language setting. */
internal interface LocalePreferenceStore {
    fun read(): LocaleOption
    fun write(option: LocaleOption)
}

internal class SharedPreferencesLocalePreferenceStore(
    context: Context,
) : LocalePreferenceStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    override fun read(): LocaleOption = LocaleOption.fromPersistedValue(
        preferences.getString(LocaleKey, null),
    )

    override fun write(option: LocaleOption) {
        preferences.edit()
            .putString(LocaleKey, option.persistedValue())
            .apply()
    }

    private companion object {
        const val PreferencesName = "calendar_showcase_preferences"
        const val LocaleKey = "locale_option"
    }
}
