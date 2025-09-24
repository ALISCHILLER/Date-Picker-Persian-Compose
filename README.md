# Date Picker Persian Compose
Modern, international-ready date and range pickers for Jetpack Compose with first-class support for the Persian (Jalali) calendar.

https://github.com/ALISCHILLER/DatePicker-Persian-Compose/assets/121736907/ae68d95f-9fc4-4a58-9d01-3a66b20774a7

## ✨ Highlights

- **Persian calendar by default** with locale-aware digits and month names
- **Single-date and range dialogs** built on Material 3 design guidelines
- **Configurable UI/UX** via `DatePickerConfig` (strings, digits mode, colors, today shortcut, etc.)
- **Powerful constraints** to enforce min/max windows, disabled dates, and business-specific validation rules
- **Strongly typed callbacks** exposing `SoleimaniDate` while keeping backward compatibility with legacy string outputs
- **Sample app** that demonstrates advanced scenarios, live toggles for localization, and design customization

## 📦 Setup

Add JitPack to your repositories and include the dependency:
```

```kotlin
// settings.gradle or build.gradle (Project)
repositories {
    maven { url = uri("https://www.jitpack.io") }
}
```

```kotlin
// module build.gradle
dependencies {
    implementation("com.github.ALISCHILLER:Date-Picker-Persian-Compose:0.0.2")
}
```

## 🚀 Quick start

```kotlin
var showPicker by remember { mutableStateOf(false) }
val selectedDate by remember { mutableStateOf<SoleimaniDate?>(null) }


@Composable
fun program () {
    if (showPicker) {
        CalendarScreen(
            onDismiss = { showPicker = false },
            onConfirm = { showPicker = false },
            onDateSelected = { date ->
                selectedDate = date
            }
        )
    }
}
```
For range selection:

```kotlin
RangeCalendarScreen(
    onDismiss = { showRange = false },
    setDate = { /* optional legacy callback */ },
    onRangeSelected = { start, end ->
        // Handle confirmed range (start <= end guaranteed)
    }
)
```
## ⚙️ Advanced configuration

Every dialog accepts a `DatePickerConfig` instance that lets you tailor the UX to your product requirements:

```kotlin
val config = DatePickerConfig(
    strings = DatePickerStrings(
        title = "Select date",
        confirm = "Confirm",
        cancel = "Cancel",
        today = "Today",
        rangeStartLabel = "Start date",
        rangeEndLabel = "End date",
    ),
    digitMode = DigitMode.Latin,              // Switch to Western digits
    showTodayAction = true,                   // Display the quick jump to today
    highlightToday = true,                    // Outline today's cell on matching month/year
    constraints = DatePickerConstraints(
        minDate = today,                      // Lock the dialog to future dates only
        maxDate = today.plusDays(30),         // ...and at most 30 days ahead
        disabledDates = generateHolidays(),   // Disable specific public holidays
        dateValidator = { candidate ->        // Custom validation for business rules
            candidate.toCalendar().getDayOfWeek() != Calendar.FRIDAY
        }
    )
)

CalendarScreen(
    onDismiss = { showPicker = false },
    onConfirm = { showPicker = false },
    config = config,
    onDateSelected = { /* typed SoleimaniDate */ }
)
```
`RangeCalendarScreen` accepts the same configuration object and also exposes `initialStartDate`, `initialEndDate`, and `onRangeSelected` for richer flows. Any constraints you configure will automatically propagate to the range dialog, preventing users from confirming invalid spans.

> ℹ️ The `DatePickerConstraints` API is extensible. It ships with built-in min/max checks, disabled-date lists, and a custom `dateValidator` callback that you can connect to your own holiday service or business calendar.

## 🧪 Sample app
Run the `app` module to explore the library interactively. The sample demonstrates:

- Single-day and range pickers
- Instant "set to today" action
- Switchable digit mode (Persian ↔ Latin) via runtime preferences
- Live toggles for the "Today" shortcut, rolling 30-day windows, weekday blocking, and recurring date blacklists
- Beautiful summaries that update automatically after each selection, including active constraint diagnostics

## 🛠️ Local development

```bash
./gradlew :calendar:assembleDebug
./gradlew :app:installDebug
```
The project targets the latest stable Compose BOM and uses Material 3. Make sure to run the Gradle tasks above before submitting contributions.


## 📄 License

Released under the [GNU GPL v3](./LICENSE).
---
Created with ❤️ for apps that need a production-grade Persian calendar experience, while still offering international teams the flexibility to localize and brand the picker for global products.

                  
