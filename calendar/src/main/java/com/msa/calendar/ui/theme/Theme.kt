package com.msa.calendar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CalendarColorTokens.Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFE8EDFF),
    secondary = CalendarColorTokens.Teal,
    onSecondary = Color(0xFF062F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFDFF7F6),
    tertiary = CalendarColorTokens.Weekend,
    onTertiary = Color.White,
    background = CalendarColorTokens.BaseDark,
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    scrim = Color(0xFF020617),
)

private val LightColorScheme = lightColorScheme(
    primary = CalendarColorTokens.Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EDFF),
    onPrimaryContainer = Color(0xFF1E2A78),
    secondary = CalendarColorTokens.Teal,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFF7F6),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = CalendarColorTokens.Weekend,
    onTertiary = Color.White,
    background = CalendarColorTokens.BaseLight,
    onBackground = CalendarColorTokens.TextPrimary,
    surface = Color.White,
    onSurface = CalendarColorTokens.TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color(0xFF020617),
)

/**
 * Material 3 theme for the library and showcase application.
 *
 * System-bar and edge-to-edge configuration intentionally belongs to the host Activity. A reusable
 * library theme must not mutate the hosting Activity window as a side effect.
 */
@Composable
fun PersianCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

/** Kept for binary/source migration of the original misspelled API. */
@Deprecated(
    message = "Use PersianCalendarTheme",
    replaceWith = ReplaceWith("PersianCalendarTheme(darkTheme, dynamicColor, content)"),
)
@Composable
fun PersionCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = PersianCalendarTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content,
)
