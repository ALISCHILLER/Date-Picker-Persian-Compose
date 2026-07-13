# معماری پروژه

## نمای کلی

پروژه یک کتابخانه Android-only با Jetpack Compose است. KMM یا Compose Multiplatform استفاده نشده و source setهایی مثل `commonMain` و `iosMain` وجود ندارند.

معماری عمداً سبک نگه داشته شده است. Date Picker یک کامپوننت UI و موتور تقویم pure است؛ افزودن Repository، UseCase یا Data layer بدون منبع داده، پیچیدگی غیرضروری ایجاد می‌کند.

```text
:app (showcase)
   └── depends on :calendar

:calendar
   ├── Public dialogs and typed selections
   ├── Presentation/state helpers
   ├── Compose UI components
   ├── Configuration/localization
   └── Pure Persian calendar engine
```

## ماژول‌ها

### `:calendar`

کتابخانه قابل‌استفاده‌مجدد:

- `com.msa.calendar`
  - Dialogها
  - مدل‌های خروجی
  - state helperهای pure
  - محاسبه labelهای دقیق میلادی برای ماه و تاریخ انتخاب‌شده
  - saverهای state
- `com.msa.calendar.ui`
  - `DatePickerConfig`
  - `DatePickerConstraints`
  - formatterها، رنگ‌ها و localization config
- `com.msa.calendar.ui.view`
  - Header و navigation
  - Grid روزها
  - انتخاب ماه و سال
  - Single و Range cells
- `com.msa.calendar.utils`
  - موتور جلالی/میلادی
  - `SoleimaniDate`
  - `PersianCalendar`
  - resource و locale helperها

### `:app`

Showcase و محیط تست دستی است. این ماژول نباید منطق تقویم یا constraintها را دوباره پیاده‌سازی کند.

## Public API

APIهای اصلی:

- `PersianDatePickerDialog`
- `PersianDateRangePickerDialog`
- `DatePickerConfig`
- `DatePickerConstraints`
- `RangeValidationMode`
- `SingleDateSelection`
- `DateRangeSelection`
- `SoleimaniDate`
- `PersianCalendar`
- `PersianCalendarLimits`

نام قدیمی `PersionCalendar` و `PersionCalendarTheme` برای سازگاری باقی مانده‌اند. کد جدید باید از نام صحیح استفاده کند.

## موتور تقویم

`PersianCalendarEngine` pure Kotlin/JVM است و به Android یا Compose وابسته نیست. تبدیل‌ها براساس الگوریتم break-year انجام می‌شوند.

محدوده موتور فقط در `PersianCalendarLimits` تعریف می‌شود:

```text
-61..3177
```

`SoleimaniDate`، API قدیمی و UI همگی از همین قرارداد استفاده می‌کنند. UI اجازه پیمایش خارج از `yearRange` یا محدوده موتور را نمی‌دهد.

## State management

Pickerها state محلی UI دارند و ViewModel عمومی تحمیل نمی‌کنند. این تصمیم برای یک Dialog کتابخانه‌ای مناسب است.

Stateهای مهم با `rememberSaveable` نگه‌داری می‌شوند:

- نوع صفحه: روز، ماه یا سال
- سال و ماه قابل مشاهده
- روز انتخاب‌شده
- شروع و پایان بازه

`PickerStateSavers.kt` تاریخ‌ها را به مقادیر primitive تبدیل می‌کند و به Parcelable/Java serialization وابسته نیست.

تغییر props ورودی باعث مقداردهی مجدد state می‌شود، اما rotation و recreation با همان ورودی، draft کاربر را حفظ می‌کند.

## Navigation

`VisibleCalendarMonth` مسئول حرکت ماه است:

- `previousOrNull(yearRange)`
- `nextOrNull(yearRange)`
- `canMovePrevious(yearRange)`
- `canMoveNext(yearRange)`

در مرزها دکمه UI واقعاً Disable می‌شود و semantics غیرفعال برای accessibility دارد. بنابراین state نامعتبر تولید نمی‌شود و Crash به موتور منتقل نمی‌شود.

## Date constraints

`DatePickerConstraints` این قواعد را ترکیب می‌کند:

1. `minDate` و `maxDate`
2. `disabledDates`
3. `dateValidator`
4. `maxRangeLength`
5. `rangeValidationMode`

در حالت پیش‌فرض `EntireRange`، endpointها و همه روزهای میانی بررسی می‌شوند. برای جلوگیری از iteration غیرضروری، اگر validator پیش‌فرض باشد، disabled dateهای میانی مستقیماً از Set پیدا می‌شوند.

`EndpointsOnly` فقط برای سازگاری با قراردادهای قدیمی وجود دارد.

## UI composition

ساختار مشترک:

```text
Dialog
 ├── Backdrop بدون semantics مزاحم
 └── Surface
      ├── Header / month navigation / quick actions
      ├── Selected date or range summary
      ├── Day, Month or Year content
      ├── Validation message
      └── Cancel / Confirm actions
```

Grid ماه در `CalendarMonthGridScaffold.kt` مشترک است. چون هر ماه همیشه دقیقاً ۴۲ slot دارد، روزها با یک Grid ثابت ۷×۶ ساخته می‌شوند؛ lazy layout فقط برای فهرست‌های واقعاً متغیر مثل انتخاب سال/ماه باقی مانده است. سلول‌ها semantics، role، state description و disabled state را منتشر می‌کنند.

## Responsive dialog contract

`DatePickerDialogMetrics.resolveAvailableSpace` قرارداد مشترک Single و Range Picker است. ورودی آن constraint واقعی Compose و `fontScale` است، نه اندازه کلی نمایشگر. این constraint بعد از اعمال `systemBarsPadding()` و `imePadding()` خوانده می‌شود؛ بنابراین Split Screen، Freeform Window، cutout، keyboard و میزبان‌های embed‌شده واقعاً در محاسبه لحاظ می‌شوند.

نکات طراحی:

- ظرف Dialog با عرض و ارتفاع دقیقِ bounded ساخته می‌شود؛ زنجیره شکننده `fillMaxHeight` یا تصمیم‌گیری براساس `LocalConfiguration` وجود ندارد.
- خروجی Metrics سه حالت Header بازه دارد: `Inline`، `Stacked` و `Condensed`. حالت Condensed در پنجره کوتاه، Landscape یا Font Scale بسیار بزرگ، ارتفاع Header را بدون مخفی‌کردن تاریخ‌ها کاهش می‌دهد.
- بدنه Day Picker یک ناحیه weight‌دار دارد. Grid ثابت ۷×۶ اندازه خانه‌ها را زیر حد خوانا کوچک نمی‌کند و فقط وقتی viewport کوتاه است در همان ناحیه scroll فعال می‌شود.
- Header روزهای هفته و Grid هر دو از `resolveCalendarGridWidth` استفاده می‌کنند؛ در نتیجه سقف سلول تبلت و هم‌راستایی هفت ستون یکسان است.
- Range Header همیشه حضور دارد و فقط محتوای endpoint عوض می‌شود؛ انتخاب روز اول باعث جهش اندازه Dialog نمی‌شود.
- مقدار تاریخ شمسی با `LayoutDirection.Ltr` نمایش داده می‌شود، درحالی‌که ترتیب کلی رابط از `WeekConfiguration` پیروی می‌کند.
- `GregorianDateLabels.kt` معادل ماه میلادی را از روز اول و آخر ماه شمسی محاسبه می‌کند؛ بنابراین عبور دسامبر/ژانویه نیز دقیق است.
- Month Picker براساس عرض واقعی ۲، ۳ یا ۴ ستون می‌سازد و Year Picker از `GridCells.Adaptive` استفاده می‌کند.
- Action Bar براساس عرض واقعی و Font Scale تصمیم می‌گیرد افقی یا عمودی باشد.

## Accessibility

- دکمه‌های ناوبری حداقل 48dp هستند.
- سطح clickable سلول روز قبل از padding داخلی اعمال می‌شود.
- Backdrop از درخت accessibility پاک شده تا یک کنترل بی‌نام ایجاد نکند.
- روزها content description شامل تاریخ، انتخاب، امروز، disabled و event دارند.
- کنترل‌های مرزی disabled semantics دارند.
- جهت فلش‌ها با `LayoutDirection` هماهنگ است.

Accessibility همچنان باید با TalkBack، Font Scale 2.0 و دستگاه 320dp تست دستی شود.

## RTL و Localization

متن‌ها از Android resource یا `DatePickerStrings` می‌آیند. `WeekConfiguration` جهت layout، شروع هفته و weekend را مشخص می‌کند.

- Persian default: شنبه، جمعه weekend، RTL
- English configuration: متن لاتین و LTR
- uppercase با `Locale.ROOT` انجام می‌شود تا رفتار به locale دستگاه وابسته نباشد.

## Edge-to-edge و Theme

Host Activity مسئول window است و `enableEdgeToEdge()` را فراخوانی می‌کند. Theme کتابخانه دیگر window میزبان را به‌عنوان side effect تغییر نمی‌دهد.

برنامه نمونه theme جدا برای day/night دارد و system barها transparent هستند. Dialog از system bar inset استفاده می‌کند.

## Performance

اصول فعلی:

- موتور pure و بدون allocationهای Android-specific
- Grid روزها ثابت و ۷×۶؛ حذف lazy bookkeeping برای مجموعه ثابت ۴۲تایی
- cache کردن Brush، Shape و لایه‌های تزئینی با `remember` و `drawWithCache`
- محاسبه یک‌باره event و selectability برای ماه قابل مشاهده در `MonthRenderSnapshot` و lookup مستقیم آرایه‌ای بر اساس شماره روز
- حذف animation، shadow، `graphicsLayer` و press state مستقل از هر روز
- فعال‌شدن vertical scroll فقط در viewport کوتاه
- Grid ثابت برای روزها و ۱۲ ماه؛ استفاده از lazy component فقط برای فهرست بزرگ سال‌ها
- جلوگیری از state نامعتبر قبل از conversion
- Baseline Profile اولیه در app و library
- Gradle build cache، parallel execution، configuration cache و Kotlin incremental compilation
- `@Immutable` روی مدل‌های پرتکرار تاریخ و خانه ماه برای کمک به stability inference در Compose
- Motion محدود به transition صفحه/ماه و قابل خاموش‌کردن با `DatePickerMotionSpec`
- سقف اندازه سلول روی پنجره‌های بزرگ برای کاهش scan distance و overdraw غیرضروری

این موارد هزینه ساخت composition و اندازه‌گیری را از نظر ساختاری کم می‌کنند. Baseline Profile دستی باید با Macrobenchmark واقعی جایگزین شود و هر ادعای عددی درباره startup یا frame time فقط پس از trace روی دستگاه نماینده معتبر است.


## Design system V8

V8 زبان بصری را به چند نقش معنایی جدا می‌کند: brand primary، accent، surface، surface variant، outline، weekend، error و selection content. این تفکیک باعث می‌شود Dark Mode مستقل باشد و مصرف‌کننده برای سفارشی‌سازی مجبور به دست‌کاری رنگ‌های تصادفی داخل کامپوننت‌ها نباشد.

- `DatePickerColors` نقش‌های انتخاب، آخرهفته، خطا و current choice را در اختیار host قرار می‌دهد.
- `DatePickerMotionSpec` زمان transitionهای سطح صفحه را کنترل می‌کند و با `enabled = false` همه آن‌ها را صفر می‌کند.
- `DatePickerActionBar` براساس عرض واقعی و Font Scale تصمیم می‌گیرد دکمه‌ها افقی یا عمودی باشند.
- Header بازه progress سه‌مرحله‌ای دارد و پیام اعتبارسنجی با live region اعلام می‌شود.
- day cells از Surface/Ripple استاندارد استفاده می‌کنند، اما shadow، graphics layer و animation مستقل ندارند.

## Testing strategy

### JVM tests

- تاریخ‌های شناخته‌شده
- Leap year و Esfand
- تبدیل رفت‌وبرگشت
- عبور از مرز سال
- constraints و نزدیک‌ترین تاریخ معتبر
- validation کل بازه
- navigation boundary
- formatting و localization helperها
- label میلادی تمام ماه‌های پشتیبانی‌شده، ماه‌های عبوری از سال و تاریخ دقیق انتخاب‌شده

### Instrumented tests

- resourceهای فارسی/انگلیسی
- backup security flag
- Compose UI و disabled navigation semantics

### CI

`.github/workflows/android.yml` دو job مستقل دارد:

```bash
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug assembleRelease
./gradlew --no-daemon :app:pixel2Api30DebugAndroidTest :calendar:pixel2Api30DebugAndroidTest
```

job دوم از Gradle Managed Device با تصویر `aosp` استفاده می‌کند تا تست‌های app و library روی emulator تکرارپذیر اجرا شوند.

## تصمیم‌های آگاهانه

- پروژه به KMM تبدیل نشده، چون نیاز و source set چندپلتفرمی ندارد.
- ViewModel داخل API کتابخانه تحمیل نشده، چون lifecycle مالک Dialog متعلق به host است.
- Legacy API حذف نشده تا breaking change ایجاد نشود.
- Signing config و secret داخل مخزن قرار نگرفته‌اند.
- ادعای Build موفق فقط پس از اجرای واقعی Gradle/CI معتبر است.
