# Dependency usage / راهنمای مصرف Dependency

## فارسی

### UI انتخاب تاریخ در Compose

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```kotlin
dependencies {
    implementation("io.github.alischiller:persian-date-picker-compose:1.0.0")
}
```

```kotlin
@Composable
fun Example() {
    val state = rememberSingleDatePickerState()
    var open by rememberSaveable { mutableStateOf(false) }

    Button(onClick = { open = true }) {
        Text(state.selectedDate?.toString() ?: "انتخاب تاریخ")
    }

    if (open) {
        PersianDatePickerDialog(
            state = state,
            onClose = { open = false },
            onSelectionConfirmed = { selection ->
                val jalaliDate: JalaliDate = selection.jalaliDate
                val gregorianDate: LocalDate = selection.gregorianDate
                open = false
            },
        )
    }
}
```

### فقط موتور تاریخ

```kotlin
dependencies {
    implementation("io.github.alischiller:persian-calendar-core:1.0.0")
}
```

```kotlin
val date = JalaliDate(1404, 1, 1)
val gregorian = date.toGregorian()
val restored = JalaliDate.fromGregorian(gregorian)
```

`persian-date-picker-compose` به Core وابستگی transitive دارد؛ آن را دوباره اضافه نکنید مگر اینکه نسخه را آگاهانه مدیریت می‌کنید.

این پروژه AGPL-3.0-only است و مصرف‌کننده باید شرایط مجوز را بررسی کند.

## English

### Compose picker UI

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```kotlin
dependencies {
    implementation("io.github.alischiller:persian-date-picker-compose:1.0.0")
}
```

```kotlin
@Composable
fun Example() {
    val state = rememberSingleDatePickerState()
    var open by rememberSaveable { mutableStateOf(false) }

    Button(onClick = { open = true }) {
        Text(state.selectedDate?.toString() ?: "Select date")
    }

    if (open) {
        PersianDatePickerDialog(
            state = state,
            onClose = { open = false },
            onSelectionConfirmed = { selection ->
                val jalaliDate: JalaliDate = selection.jalaliDate
                val gregorianDate: LocalDate = selection.gregorianDate
                open = false
            },
        )
    }
}
```

### Core-only calendar engine

```kotlin
dependencies {
    implementation("io.github.alischiller:persian-calendar-core:1.0.0")
}
```

```kotlin
val date = JalaliDate(1404, 1, 1)
val gregorian = date.toGregorian()
val restored = JalaliDate.fromGregorian(gregorian)
```

The Compose artifact exposes Core transitively; do not add it again unless you intentionally manage its version.

This project is AGPL-3.0-only. Consumers must review the license requirements.
