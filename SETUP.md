# راه‌اندازی و انتشار

این پروژه فقط Android است و دو ماژول `:calendar` و `:app` دارد. برای iOS، Desktop یا Web تنظیم Build وجود ندارد.

## محیط توسعه

### پیش‌نیازها

- Android Studio پایدار و جدید
- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools و Platform Tools
- دسترسی به Google Maven، Maven Central و Gradle Distribution

نسخه Gradle لازم از Wrapper پروژه دریافت می‌شود؛ Gradle جداگانه نصب نکنید.

### Clone و Sync

```bash
git clone <repository-url>
cd Date-Picker-Persian-Compose-main
./gradlew --version
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

روی Windows:

```powershell
.\gradlew.bat --version
.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug
```

### `local.properties`

Android Studio معمولاً این فایل را می‌سازد. فایل را commit نکنید.

Windows:

```properties
sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
```

macOS:

```properties
sdk.dir=/Users/YOUR_USER/Library/Android/sdk
```

Linux:

```properties
sdk.dir=/home/YOUR_USER/Android/Sdk
```

### تنظیمات سرعت Build

پروژه به‌صورت پیش‌فرض از Gradle build cache، parallel execution، configuration cache، file-system watching و Kotlin incremental compilation استفاده می‌کند. برای یک build تشخیصی بدون configuration cache:

```bash
./gradlew --no-configuration-cache clean assembleDebug
```

اگر یک plugin محلی با configuration cache سازگار نبود، ابتدا همان فرمان را اجرا و گزارش دقیق plugin را بررسی کنید؛ cache را بدون دلیل برای کل تیم خاموش نکنید.

### اجرای برنامه نمونه

1. پروژه را در Android Studio باز کنید.
2. منتظر Gradle Sync بمانید.
3. Run configuration ماژول `app` را انتخاب کنید.
4. روی emulator یا دستگاه Android 8 به بالا اجرا کنید.

### فرمان‌های بررسی

```bash
# Unit tests
./gradlew testDebugUnitTest

# Android Lint
./gradlew lintDebug

# Debug APK
./gradlew assembleDebug

# Instrumented tests روی device/emulator متصل
./gradlew connectedDebugAndroidTest

# Instrumented tests روی Gradle Managed Device پروژه
./gradlew :app:pixel2Api30DebugAndroidTest :calendar:pixel2Api30DebugAndroidTest

# همه بررسی‌های JVM/Lint/Build اصلی CI
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

گزارش‌ها معمولاً در مسیرهای زیر ساخته می‌شوند:

- `calendar/build/reports/tests/`
- `app/build/reports/tests/`
- `calendar/build/reports/lint-results-debug.html`
- `app/build/reports/lint-results-debug.html`

## Configuration

### نسخه‌ها

نسخه‌های plugin، dependency و SDK در `gradle/libs.versions.toml` قرار دارند. نسخه‌ها را دسته‌ای ارتقا ندهید. ابتدا baseline سبز بگیرید، سپس هر گروه را جدا ارتقا دهید و دوباره Test/Lint/Build را اجرا کنید.

### تنظیم Date Picker

ورودی اصلی `DatePickerConfig` است:

- `strings` — متن‌ها و ترجمه‌ها
- `colors` — رنگ‌های picker
- `digitMode` — ارقام فارسی یا لاتین
- `weekConfiguration` — شروع هفته، تعطیلات و جهت UI
- `yearRange` — محدوده سال قابل پیمایش
- `constraints` — حداقل/حداکثر، روزهای غیرفعال و validator
- `quickActions` — امروز، پاک‌کردن یا رفتن به تاریخ مشخص
- `eventIndicator` — نشان رویداد روی روز
- `dateFormatter` و `rangeFormatter` — خروجی نمایشی
- `showGregorianDateHints` — نمایش ماه/تاریخ میلادی متناظر در Header و صفحه انتخاب ماه؛ مقدار پیش‌فرض `true` است
- `motionSpec` — فعال/غیرفعال‌کردن و تنظیم زمان transitionهای سطح صفحه و ماه
- `enableHaptics` — بازخورد لمسی سبک هنگام انتخاب روز؛ مقدار پیش‌فرض `true` است


نمونه تنظیم Motion برای محیط‌های کم‌توان یا تست UI:

```kotlin
DatePickerConfig(
    motionSpec = DatePickerMotionSpec(enabled = false),
    enableHaptics = false,
)
```

`yearRange` باید داخل بازه پشتیبانی موتور، یعنی `-61..3177` باشد. اگر `yearRange` با `minDate/maxDate` اشتراک نداشته باشد، پیکربندی با خطای واضح رد می‌شود.

### Range validation

رفتار امن پیش‌فرض:

```kotlin
DatePickerConstraints(
    rangeValidationMode = RangeValidationMode.EntireRange,
)
```

این حالت تمام روزهای بازه را بررسی می‌کند. برای سازگاری با رفتار قدیمی:

```kotlin
DatePickerConstraints(
    rangeValidationMode = RangeValidationMode.EndpointsOnly,
)
```

### Localization

برنامه نمونه در `MainActivity` این مقداردهی را انجام می‌دهد:

```kotlin
CalendarResources.initialize(applicationContext)
```

در کتابخانه مصرف‌کننده می‌تواند `DatePickerStrings`، formatterها و `WeekConfiguration` را مستقیماً تزریق کند. متن قابل نمایش را hardcode نکنید.

### Environment variables

پروژه API، دیتابیس، token یا secret ندارد؛ بنابراین environment variable اجباری وجود ندارد. هیچ keystore یا password را داخل Git قرار ندهید.

## Debug و تست دستی

حداقل این ماتریس را بررسی کنید:

- زبان فارسی و انگلیسی
- RTL و LTR
- ارقام فارسی و لاتین
- Light و Dark theme
- Android 8، Android 12 و Android 15 یا جدیدتر
- عرض 320/340/360/412dp، تبلت و Landscape
- Font Scale برابر 1.0، 1.3 و 2.0
- در Range Picker هر دو مقدار عددی شروع و پایان بدون ellipsis یا جابه‌جایی RTL دیده شوند
- برای فروردین، hint میلادی دو ماه `مارس – آوریل` را نشان دهد و برای دی، عبور `دسامبر – ژانویه` با دو سال درست نمایش داده شود
- در صفحه انتخاب ماه، هر کارت ماه شمسی نام ماه یا ماه‌های میلادی متناظر را بدون کوچک‌شدن متن اصلی نشان دهد
- بعد از انتخاب تاریخ شروع، ارتفاع Dialog و اندازه اعداد روزها تغییر ناگهانی نکند
- در Landscape، Grid روزها scroll شود و Header/دکمه‌ها قابل دسترس بمانند
- TalkBack و navigation با keyboard/D-pad
- اولین و آخرین ماه `yearRange`
- min/max date در وسط ماه
- disabled date در داخل بازه
- process recreation و rotation هنگام انتخاب
- release build با R8

## Build تولید

### Signing

فایل keystore را بیرون مخزن نگه دارید. یک الگوی رایج:

```properties
RELEASE_STORE_FILE=/secure/path/release.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=***
RELEASE_KEY_PASSWORD=***
```

این مقادیر را از CI secret یا فایل محلی خارج از Git بخوانید. پروژه عمداً signing credential نمونه ندارد.

### APK و AAB

```bash
./gradlew clean assembleRelease
./gradlew bundleRelease
```

خروجی‌های معمول:

- `app/build/outputs/apk/release/`
- `app/build/outputs/bundle/release/`

### R8 و Resource Shrinking

Release برنامه نمونه با `isMinifyEnabled = true` و `isShrinkResources = true` ساخته می‌شود. بعد از هر تغییر public API یا reflection، build release را روی دستگاه واقعی اجرا کنید. کتابخانه reflection یا serialization مبتنی بر نام کلاس ندارد و در وضعیت فعلی consumer rule ویژه‌ای لازم نیست.

### Baseline Profile

فایل‌های اولیه:

- `app/src/main/baseline-prof.txt`
- `calendar/src/main/baseline-prof.txt`

این فایل‌ها مسیرهای اصلی را پوشش می‌دهند، اما جای اندازه‌گیری واقعی را نمی‌گیرند. پیش از انتشار عمومی:

1. Macrobenchmark/Baseline Profile generator را روی دستگاه نماینده اجرا کنید.
2. Startup و بازکردن picker، پیمایش ماه و Confirm را پوشش دهید.
3. profile تولیدشده را جایگزین profile دستی کنید.
4. تفاوت startup و frame timing را اندازه‌گیری کنید.

## بررسی Performance روی دستگاه واقعی

برای تأیید اثر بهینه‌سازی‌ها، حداقل سناریوهای زیر را با Macrobenchmark یا System Trace اندازه‌گیری کنید:

1. cold start برنامه نمونه؛
2. اولین بازشدن Single Picker و Range Picker؛
3. حرکت سریع میان ۱۲ ماه؛
4. انتخاب شروع و پایان یک بازه بلند؛
5. بازکردن انتخاب سال و scroll سریع؛
6. تکرار همه موارد در Light، Dark، RTL و Font Scale بزرگ.

در گزارش، median و P90 frame duration، تعداد jank frameها و startup timing را ثبت کنید. مقایسه باید روی یک دستگاه، یک build type و پس از warm-up یکسان انجام شود.

## انتشار Google Play

1. `app-version-code` و `app-version-name` را در version catalog افزایش دهید؛ نسخه این خروجی `1.5 (6)` است.
2. Unit Test، Lint، Debug و Release Build را سبز کنید.
3. AAB امضاشده بسازید.
4. ابتدا در Internal testing منتشر کنید.
5. Pre-launch report و crashها را بررسی کنید.
6. سپس rollout مرحله‌ای انجام دهید.

## خطاهای رایج

### Gradle Wrapper دانلود نمی‌شود

خطاهایی مثل `UnknownHostException: services.gradle.org` به شبکه، proxy یا firewall مربوط‌اند. Wrapper و dependencyها باید از منبع رسمی یا mirror سازمانی معتبر دریافت شوند.

### Android SDK پیدا نمی‌شود

`ANDROID_HOME` یا `sdk.dir` را بررسی کنید و مطمئن شوید Platform 36 نصب است.

### تاریخ اولیه نمایش داده نمی‌شود

بررسی کنید:

- سال داخل `yearRange` باشد.
- تاریخ با `minDate/maxDate` سازگار باشد.
- تاریخ توسط `disabledDates` یا `dateValidator` رد نشده باشد.

Picker نزدیک‌ترین تاریخ معتبر داخل محدوده را انتخاب می‌کند.

### پایان بازه قابل انتخاب نیست

در حالت `EntireRange`، وجود حتی یک روز غیرفعال بین شروع و پایان باعث رد پایان می‌شود. اگر قرارداد محصول فقط endpointها را بررسی می‌کند، `EndpointsOnly` را صریح تنظیم کنید.

### Layout روی صفحه کوچک فشرده است

روی عرض‌های بسیار کوچک، فضای هفت ستون محدود است. پروژه padding افقی را تطبیق داده و سطح clickable را روی کل سلول نگه می‌دارد؛ بااین‌حال حتماً دستگاه 320dp و TalkBack را تست کنید.

## چک‌لیست Release

- [ ] CI سبز است.
- [ ] `testDebugUnitTest` و `lintDebug` بدون خطا اجرا شده‌اند.
- [ ] Managed Device tests یا `connectedDebugAndroidTest` روی emulator/device اجرا شده است.
- [ ] APK/AAB release امضاشده ساخته شده است.
- [ ] R8 و resource shrinking بررسی شده‌اند.
- [ ] فارسی، انگلیسی، RTL و LTR تست شده‌اند.
- [ ] معادل میلادی ماه جاری، ماه‌های عبوری از سال و تاریخ‌های Range بررسی شده‌اند.
- [ ] Light/Dark و Font Scale بالا تست شده‌اند.
- [ ] مرز سال، min/max و disabled dates تست شده‌اند.
- [ ] Rotation و process recreation تست شده‌اند.
- [ ] TalkBack و touch targetها بررسی شده‌اند.
- [ ] Baseline Profile روی دستگاه نماینده اندازه‌گیری شده است.
- [ ] متن AGPL و noticeهای لازم همراه انتشار هستند.
