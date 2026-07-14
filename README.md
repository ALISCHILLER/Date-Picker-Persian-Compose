<div align="center">

# Persian Date Picker for Jetpack Compose

A production-oriented Jalali/Persian date and date-range picker for Android,  
built with Kotlin, Jetpack Compose and Material 3.

[![Android CI](https://github.com/ALISCHILLER/Date-Picker-Persian-Compose/actions/workflows/android.yml/badge.svg)](https://github.com/ALISCHILLER/Date-Picker-Persian-Compose/actions/workflows/android.yml)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-AGPL--3.0-orange.svg)](LICENSE.md)

[Features](#features) •
[Getting Started](#getting-started) •
[Usage](#usage) •
[Configuration](#configuration) •
[Architecture](#architecture) •
[Testing](#testing)

</div>

---

## Overview

**Persian Calendar Compose** is an Android-only library for selecting a single Jalali date or a Jalali date range with strongly typed results.

It includes a reusable `:calendar` module and a standalone `:app` showcase. The calendar engine is implemented in pure Kotlin/JVM and does not depend on Android calendar APIs or ICU for Jalali/Gregorian conversion.

> This repository targets native Android and Jetpack Compose.  
> For Kotlin Multiplatform and Compose Multiplatform, see
> [PersianDatePicker-Kmm-Enterprise](https://github.com/ALISCHILLER/PersianDatePicker-Kmm-Enterprise).

---

## Features

### Date selection

- Single-date picker
- Date-range picker
- Strongly typed selection results
- Jalali-to-Gregorian and Gregorian-to-Jalali conversion
- Ordered range results
- Safe navigation within the configured year range
- State restoration across rotation and activity recreation

### Constraints and validation

- Minimum and maximum selectable dates
- Disabled dates
- Custom date validator
- Maximum range length
- Full-range validation
- Optional endpoint-only validation for legacy behavior
- Clear inline validation messages

### Localization

- Persian and English text
- RTL and LTR layouts
- Persian and Latin digits
- Configurable first day of week and weekend
- Accurate Gregorian month hints
- Correct handling of Gregorian year boundaries

### Responsive UI

- Compact phones
- Tablets
- Foldables
- Landscape mode
- Split-screen and freeform windows
- Large font scales
- Keyboard and IME-aware sizing
- Adaptive month and year grids

### Accessibility

- TalkBack-friendly semantics
- Minimum 48dp navigation targets
- Selected, disabled, today and event state descriptions
- Accessible boundary navigation
- Keyboard and D-pad support
- Inline validation announcements

### Performance

- Fixed 7×6 calendar grid
- No unnecessary lazy layout for the 42 day slots
- Cached brushes and shapes
- Month-level render snapshots
- Direct day lookup
- Controlled page-level motion
- Optional haptic feedback
- Baseline Profile placeholders for the app and library modules

---

## Project Structure

```text
Date-Picker-Persian-Compose
├── app/                    # Showcase application
├── calendar/               # Reusable Android library
├── gradle/                 # Version catalog and wrapper files
├── .github/workflows/      # Android CI
├── ARCHITECTURE.md         # Architecture and design decisions
├── SETUP.md                # Setup, build and release guide
└── LICENSE.md              # AGPL-3.0 license
```

### Modules

| Module | Purpose |
|---|---|
| `:calendar` | Public picker APIs, calendar engine, state helpers, localization, constraints and Compose UI |
| `:app` | Manual showcase for single-date and date-range picker flows |

---

## Requirements

| Item | Version |
|---|---:|
| Kotlin | `2.2.20` |
| Android Gradle Plugin | `8.10.1` |
| Gradle Wrapper | `8.11.1` |
| Compose BOM | `2026.06.00` |
| Compile SDK | `36` |
| Target SDK | `35` |
| Min SDK | `26` |
| Java | `17` |

---

## Getting Started

### Clone and verify

```bash
git clone https://github.com/ALISCHILLER/Date-Picker-Persian-Compose.git
cd Date-Picker-Persian-Compose

./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

On Windows:

```powershell
git clone https://github.com/ALISCHILLER/Date-Picker-Persian-Compose.git
cd Date-Picker-Persian-Compose

.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug
```

Open the project in Android Studio and run the `app` configuration to explore the showcase.

### Use as a local source module

A public Maven artifact is not configured yet. Until one is published, the library can be consumed as a local source module.

Add the repository as a Git submodule:

```bash
git submodule add \
  https://github.com/ALISCHILLER/Date-Picker-Persian-Compose.git \
  third_party/persian-calendar-compose
```

Add the library module in `settings.gradle.kts`:

```kotlin
include(":persian-calendar")

project(":persian-calendar").projectDir =
    file("third_party/persian-calendar-compose/calendar")
```

Add the dependency to the consuming application:

```kotlin
dependencies {
    implementation(project(":persian-calendar"))
}
```

---

## Usage

### Single-date picker

```kotlin
@Composable
fun SingleDatePickerExample() {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<SoleimaniDate?>(null) }

    Button(onClick = { showPicker = true }) {
        Text("Select date")
    }

    if (showPicker) {
        PersianDatePickerDialog(
            initialDate = selectedDate,
            onClose = {
                showPicker = false
            },
            onSelectionConfirmed = { selection ->
                selectedDate = selection.date
                showPicker = false

                // selection.date
                // selection.gregorianDate
                // selection.formattedDate
            },
        )
    }
}
```

### Date-range picker

```kotlin
@Composable
fun DateRangePickerExample() {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf<DateRangeSelection?>(null) }

    Button(onClick = { showPicker = true }) {
        Text("Select range")
    }

    if (showPicker) {
        PersianDateRangePickerDialog(
            initialStartDate = selectedRange?.startDate,
            initialEndDate = selectedRange?.endDate,
            onClose = {
                showPicker = false
            },
            onSelectionConfirmed = { selection ->
                selectedRange = selection
                showPicker = false
            },
        )
    }
}
```

> The range result is ordered from the earlier date to the later date.

---

## Configuration

Use `DatePickerConfig` to customize behavior and appearance.

```kotlin
val config = DatePickerConfig(
    yearRange = 1380..1450,
    showGregorianDateHints = true,
    enableHaptics = true,
    motionSpec = DatePickerMotionSpec(
        enabled = true,
    ),
    constraints = DatePickerConstraints(
        minDate = SoleimaniDate(1400, 1, 1),
        maxDate = SoleimaniDate(1450, 12, 29),
        disabledDates = setOf(
            SoleimaniDate(1405, 1, 13),
        ),
        maxRangeLength = 30,
        dateValidator = { date ->
            // Add domain-specific validation here.
            true
        },
    ),
)
```

Pass the configuration to either picker:

```kotlin
PersianDatePickerDialog(
    config = config,
    onClose = { /* close */ },
    onSelectionConfirmed = { selection ->
        // Consume typed selection.
    },
)
```

### Range validation

The safe default validates the complete range, including all intermediate days:

```kotlin
DatePickerConstraints(
    rangeValidationMode = RangeValidationMode.EntireRange,
)
```

Use endpoint-only validation only when compatibility with older behavior is required:

```kotlin
DatePickerConstraints(
    rangeValidationMode = RangeValidationMode.EndpointsOnly,
)
```

### Disable motion and haptics

Useful for low-power environments, deterministic UI tests or reduced-motion experiences:

```kotlin
DatePickerConfig(
    motionSpec = DatePickerMotionSpec(enabled = false),
    enableHaptics = false,
)
```

---

## Public API

Main public types:

```text
PersianDatePickerDialog
PersianDateRangePickerDialog
DatePickerConfig
DatePickerConstraints
DatePickerMotionSpec
RangeValidationMode
SingleDateSelection
DateRangeSelection
SoleimaniDate
PersianCalendar
PersianCalendarLimits
```

The legacy names `PersionCalendar` and `PersionCalendarTheme` remain available for backward compatibility. New integrations should use the correctly spelled APIs.

---

## Architecture

The project intentionally uses a lightweight architecture.

A date-picker library does not need repositories, use cases or a data layer when it has no external data source. The reusable module is divided around public APIs, pure calendar logic, state helpers, configuration and Compose UI.

```text
:app
 └── depends on :calendar

:calendar
 ├── Public dialogs and typed results
 ├── State and presentation helpers
 ├── Compose UI components
 ├── Configuration and localization
 └── Pure Jalali calendar engine
```

The picker keeps local UI state instead of forcing a library-specific `ViewModel`. Important values are stored with `rememberSaveable`, using primitive savers instead of Parcelable or Java serialization.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the complete design documentation.

---

## Testing

Run the main verification suite:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Run instrumented tests on a connected device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

Run the configured Gradle Managed Device tests:

```bash
./gradlew \
  :app:pixel2Api30DebugAndroidTest \
  :calendar:pixel2Api30DebugAndroidTest
```

The test strategy includes:

- Jalali/Gregorian conversion tests
- Calendar boundary tests
- Constraint and range validation tests
- Formatting tests
- State-helper tests
- Resource and security configuration tests
- Compose UI navigation tests
- Persian and Latin date rendering tests
- Gregorian hint tests
- Managed-device instrumented tests

Android CI runs:

- Gradle Wrapper validation
- Unit tests
- Android Lint
- Debug build
- Minified release build
- Managed-device instrumented tests
- Failure report uploads

---

## Build and Release Status

The repository contains a complete Android CI workflow. Do not describe a build as passing until the workflow has completed successfully for the target commit.

The current Baseline Profile files are initial placeholders. Before a production release, regenerate and measure them with Macrobenchmark on representative devices.

See [SETUP.md](SETUP.md) for:

- Development requirements
- Build commands
- Manual test matrix
- Signing
- R8 and resource shrinking
- Baseline Profile generation
- Performance measurement
- Release checks
- Troubleshooting

---

## Roadmap

- [ ] Publish the library to Maven Central or GitHub Packages
- [ ] Add public semantic versioning for the library module
- [ ] Add screenshots and an animated demo
- [ ] Add generated API documentation
- [ ] Add a changelog and tagged releases
- [ ] Generate production Baseline Profiles with Macrobenchmark
- [ ] Publish test and coverage reports
- [ ] Add a migration guide for legacy API names

---

## License

This project is licensed under the
[GNU Affero General Public License v3.0](LICENSE.md).

Review the license terms before embedding the library in another application, especially when distributing modified versions.

---

## Author

Developed and maintained by
[Ali Soleimani](https://github.com/ALISCHILLER).

Contributions, bug reports and improvement proposals are welcome through GitHub Issues and Pull Requests.
