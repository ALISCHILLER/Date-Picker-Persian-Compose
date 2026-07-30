# Migration guide

## Existing source-module users to Maven artifacts

Replace:

```kotlin
implementation(project(":calendar"))
```

with:

```kotlin
implementation("io.github.alischiller:persian-date-picker-compose:1.0.0")
```

The Compose artifact brings `persian-calendar-core` transitively.

## Legacy date model to Core model

Existing code remains valid:

```kotlin
val date: SoleimaniDate = selection.date
```

New domain/data code can use the Android-free model:

```kotlin
val date: JalaliDate = selection.jalaliDate
val converted = legacyDate.toJalaliDate()
val compatibilityDate = SoleimaniDate.from(date)
```

Do not migrate all call sites mechanically. Keep `SoleimaniDate` at compatibility/UI boundaries and use `JalaliDate` where Android-free domain logic is valuable.

## Local selection variables to state holders

Existing overload:

```kotlin
PersianDatePickerDialog(
    initialDate = selectedDate,
    onClose = { visible = false },
    onSelectionConfirmed = { selectedDate = it.date },
)
```

State-holder overload:

```kotlin
val constraints = remember { DatePickerConstraints() }
val state = rememberSingleDatePickerState(constraints = constraints)

PersianDatePickerDialog(
    state = state,
    config = DatePickerConfig(constraints = constraints),
    onClose = { visible = false },
    onSelectionConfirmed = { result ->
        val coreDate = result.jalaliDate
    },
)
```

The state holder owns the effective validation constraints. Transient month/page state remains internal to the dialog.

## Misspelled legacy names

Use:

```kotlin
PersianCalendar
PersianCalendarTheme
```

instead of:

```kotlin
PersionCalendar
PersionCalendarTheme
```

Legacy names remain for compatibility and should not be used in new integrations.

## Breaking-change policy

The first published `1.0.0` establishes the binary contract. After publication:

- fixes use patch releases;
- backward-compatible features use minor releases;
- removals or incompatible signature changes require a major release;
- deprecated API remains through a documented migration window.

## Sample application package correction

The unpublished showcase application package changed from the misspelled `com.msa.persioncalendar` to `com.msa.persiancalendar`. This does not change either Maven coordinate or the library namespace `com.msa.calendar`.

Because Android treats the corrected `applicationId` as a different application, uninstall an older locally installed showcase build before testing the new one if both package variants cause confusion. No consumer-library migration is required.
