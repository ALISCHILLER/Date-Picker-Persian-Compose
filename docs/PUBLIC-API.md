# Public API contract

## Maven artifacts

```kotlin
implementation("io.github.alischiller:persian-calendar-core:1.0.0")
implementation("io.github.alischiller:persian-date-picker-compose:1.0.0")
```

The Compose artifact depends transitively on the Core artifact. Applications that only need date conversion should prefer Core.

## Core API

- `JalaliDate`
- `JalaliCalendar`
- `JalaliDateRange`
- `JalaliCalendarLimits`
- `CalendarDigits`
- `JalaliDateParseResult` and `JalaliDateParseError`

Core is Android-free and may be used in repositories, domain models, workers, services, and JVM tools.

## Compose API

- `PersianDatePickerDialog`
- `PersianDateRangePickerDialog`
- `rememberSingleDatePickerState`
- `rememberDateRangePickerState`
- `SingleDatePickerState` and `SingleDatePickerEvent`
- `DateRangePickerState` and `DateRangePickerEvent`
- `DatePickerConfig`
- `DatePickerConstraints`
- `DatePickerStrings`
- `DatePickerColors`
- `DatePickerMotionSpec`
- `SingleDateSelection`
- `DateRangeSelection`
- `SoleimaniDate`

## API evolution rules

1. Public declarations need KDoc and a deliberate visibility modifier in new Core code.
2. A published signature is not renamed, removed, or reordered in a minor or patch release.
3. New optional behavior is added with overloads or trailing default parameters after compatibility review.
4. Deprecation includes a replacement and remains for at least one minor release.
5. Behavioral changes to conversion, constraints, ordering, or formatting require regression tests and changelog notes.
6. Internal UI components, previews, reducers, and rendering snapshots are not public API.

## Stateful Compose usage

```kotlin
val config = DatePickerConfig()
val state = rememberSingleDatePickerState(
    initialSelection = selectedDate,
    constraints = config.constraints,
)

PersianDatePickerDialog(
    state = state,
    config = config,
    onClose = { showPicker = false },
    onSelectionConfirmed = {
        selectedDate = it.date
        showPicker = false
    },
)
```

The state holder owns the effective constraints. The stateful dialog overload copies those constraints into the effective UI configuration, preventing divergent validation rules.

Selection results expose both compatibility models (`date`, `startDate`, `endDate`) and Android-free Core models (`jalaliDate`, `jalaliRange`).

## State construction

`SingleDatePickerState` and `DateRangePickerState` have public constructors with defaults and Java overloads for hosts that own state outside composition. Compose callers normally use `rememberSingleDatePickerState` and `rememberDateRangePickerState` so primitive Saver-based restoration is configured automatically.

## Consumer contract checks

`samples/maven-consumer` compiles two independent consumers against Maven-local publications:

- an Android Compose application consuming `persian-date-picker-compose`;
- a Java 17 application consuming `persian-calendar-core`, including conversion and typed parsing.

These checks validate POM transitivity and public signatures rather than relying on project-to-project dependencies.
