# معماری پروژه

## هدف

این مخزن یک کتابخانه قابل انتشار است، نه یک اپلیکیشن چندلایه با منبع داده. معماری عمداً فقط مرزهایی را ایجاد می‌کند که مسئولیت واقعی دارند؛ Repository، UseCase، DI یا ViewModel مصنوعی به پروژه تحمیل نشده‌اند.

```text
:samples/maven-consumer  (مصرف‌کننده مستقل artifact منتشرشده)
           │
           ▼
:app  ───────────────►  :calendar  ───────────────►  :calendar-core
Showcase                 Android/Compose API          Pure Kotlin/JVM
```

قانون وابستگی یک‌طرفه است:

- `calendar-core` هیچ وابستگی به Android، AndroidX، Compose یا ماژول‌های بالاتر ندارد.
- `calendar` فقط UI، localization اندروید و سازگاری API قدیمی را نگه می‌دارد.
- `app` فقط مصرف‌کننده API عمومی است و نباید منطق تبدیل تاریخ را دوباره پیاده‌سازی کند.
- `samples/maven-consumer` از Project Dependency استفاده نمی‌کند؛ artifactهای `mavenLocal()` را مانند یک مصرف‌کننده واقعی کامپایل می‌کند.

## ماژول `:calendar-core`

Artifact:

```text
io.github.alischiller:persian-calendar-core:<version>
```

این ماژول Pure Kotlin/JVM است و شامل موارد زیر است:

- `JalaliDate`: مدل immutable و قابل مقایسه تاریخ
- `JalaliCalendar`: تبدیل جلالی/میلادی، طول ماه و leap year
- `JalaliDateRange`: بازه مرتب و inclusive
- `CalendarDigits`: نرمال‌سازی ارقام فارسی، عربی و لاتین
- `JalaliCalendarLimits`: قرارداد محدوده الگوریتم
- `internal/JalaliAlgorithm`: جزئیات الگوریتم که وارد API عمومی نمی‌شود

### قواعد Core

1. Import از `android.*`، `androidx.*` و Compose ممنوع است.
2. API عمومی با `explicitApi()` کامپایل می‌شود.
3. Warningهای Kotlin در Core خطا محسوب می‌شوند.
4. مدل‌ها immutable و عملیات تقویم deterministic هستند.
5. زمان جاری فقط از طریق `ZoneId` صریح یا مقدار پیش‌فرض سیستم دریافت می‌شود.
6. Parserها throw نمی‌کنند و نسخه `parseOrNull` دارند؛ constructorها invariant را با `require` حفظ می‌کنند.

## ماژول `:calendar`

Artifact اصلی UI:

```text
io.github.alischiller:persian-date-picker-compose:<version>
```

این artifact با `api(project(":calendar-core"))` به Core وابسته است؛ بنابراین مصرف‌کننده Compose به‌صورت transitively به مدل‌های Core دسترسی دارد.

مسئولیت‌های آن:

- Dialogهای type-safe
- State Holder و eventهای UDF
- محدودیت تاریخ و بازه
- Material 3 UI و Design Tokens
- RTL/LTR و resourceهای فارسی/انگلیسی
- Accessibility semantics
- سازگاری با APIهای قدیمی `PersionCalendar` و `CalendarScreen`

### API توصیه‌شده

```text
PersianDatePickerDialog
PersianDateRangePickerDialog
rememberSingleDatePickerState
rememberDateRangePickerState
SingleDatePickerState / SingleDatePickerEvent
DateRangePickerState / DateRangePickerEvent
DatePickerConfig / DatePickerConstraints
SingleDateSelection / DateRangeSelection
SoleimaniDate
```

`CalendarScreen` و `RangeCalendarScreen` API سطح پایین و deprecated هستند. توسعه جدید باید از wrapperهای typed استفاده کند.

## State و UDF

State تأییدشده و قابل کنترل Picker در State Holder ساده Compose قرار می‌گیرد، نه ViewModel اجباری. State گذرای نمایش مانند صفحه ماه و mode داخل Dialog نزدیک محل مصرف نگه‌داری می‌شود:

```text
User interaction
      │
      ▼
DatePickerEvent
      │
      ▼
StateHolder.dispatch(event)
      │
      ▼
Immutable observable state
      │
      ▼
Compose UI
```

اصول:

- State پایین می‌رود و Event بالا می‌آید.
- State Holder منبع انتخاب تأییدشده مصرف‌کننده است؛ state گذرای قبل از Confirm داخل Dialog باقی می‌ماند.
- `rememberSaveable` و Saver primitive، state را در recreation حفظ می‌کنند.
- Constraintها قبل از mutation بررسی می‌شوند.
- Range همیشه مرتب است و در حالت `EntireRange` تمام روزهای میانی validate می‌شوند.
- کتابخانه Lifecycle، ViewModelStoreOwner یا DI framework به مصرف‌کننده تحمیل نمی‌کند.

## سازگاری بین Core و API قدیمی

`PersianCalendarEngine` در ماژول Compose فقط یک facade داخلی است و تمام محاسبات را به `JalaliCalendar` واگذار می‌کند. در نتیجه یک الگوریتم واحد منبع حقیقت است.

`SoleimaniDate` برای سازگاری API فعلی باقی مانده و تبدیل مستقیم دارد:

```kotlin
val coreDate = soleimaniDate.toJalaliDate()
val composeDate = SoleimaniDate.from(coreDate)
```

## UI و Compose

- Material 3 تنها Design System UI است.
- Grid روز ثابت ۷×۶ است و برای ۴۲ سلول از Lazy layout استفاده نمی‌شود.
- لیست سال‌ها می‌تواند Lazy باشد چون اندازه متغیر و بزرگ دارد.
- State readها نزدیک‌ترین سطح مصرف انجام می‌شوند.
- Brush، Shape و snapshot ماه cache می‌شوند.
- انیمیشن‌ها در سطح transition صفحه/ماه محدود و با `DatePickerMotionSpec` قابل خاموش‌کردن‌اند.
- `Modifier` عمومی اولین پارامتر اختیاری UI است و به node ریشه منتقل می‌شود.

## Localization و Accessibility

- کلیدهای `values` و `values-fa` با script معماری parity-check می‌شوند.
- فارسی RTL و انگلیسی LTR است.
- تاریخ‌های عددی در فضای mixed-direction با جهت کنترل‌شده نمایش داده می‌شوند.
- Navigation targetها حداقل 48dp هستند.
- Selected، disabled، today، event و خطا فقط با رنگ منتقل نمی‌شوند و semantics متنی دارند.
- پیام validation با live region اعلام می‌شود.

## Public API و Versioning

- مختصات Maven و نسخه در `gradle.properties` متمرکزند.
- Core و Compose در هر release نسخه یکسان دارند.
- تغییر breaking فقط در Major version مجاز است.
- API قدیمی ابتدا deprecated و در Major بعدی حذف می‌شود.
- جزئیات implementation باید `internal` یا `private` باشند.
- راهنمای API رسمی در [`docs/PUBLIC-API.md`](docs/PUBLIC-API.md) است.

## Verification

### بدون Android SDK

```bash
./scripts/verify-core-standalone.sh
./scripts/verify-architecture.sh
```

### Gradle

```bash
./gradlew :calendar-core:check
./gradlew :calendar:testDebugUnitTest :app:testDebugUnitTest
./gradlew :calendar:lintDebug :app:lintDebug
./gradlew :calendar:assembleRelease :app:assembleRelease
```

### قرارداد انتشار واقعی

```bash
./scripts/verify-maven-consumer.sh
```

این script هر دو artifact را در `mavenLocal()` منتشر و پروژه مستقل `samples/maven-consumer` را build می‌کند. این مرحله خطاهای POM، dependency transitiveness و API مصرف‌کننده را آشکار می‌کند که `implementation(project(...))` قادر به کشف آن‌ها نیست.

## CI و Supply Chain

- Gradle Wrapper validation
- Unit، Lint، Debug و minified Release build
- Managed-device tests
- Maven consumer smoke test
- Dependency Review برای PRها
- CodeQL برای Java/Kotlin
- Dependabot گروه‌بندی‌شده برای Gradle و GitHub Actions
- Secretها فقط از GitHub Secrets یا `~/.gradle/gradle.properties`
- AGPL-3.0 در هر دو POM اعلام می‌شود

## تصمیم‌های آگاهانه

- پروژه KMP نشده است؛ نیاز فعلی Android و JVM است.
- `calendar-core` از ابتدا Android-free طراحی شده تا مسیر KMP آینده بسته نشود، اما Target غیرفعال به‌عنوان قابلیت موجود معرفی نمی‌شود.
- ViewModel و DI در API کتابخانه وجود ندارند.
- Architecture پیچیده‌تر فقط با نیاز واقعی محصول یا داده اضافه می‌شود.
- ادعای Production-ready فقط پس از Build، Runtime، Release، UI tests و انتشار واقعی معتبر است.


## Validation and error contracts

Normal user-input failures are represented with `JalaliDateParseResult` and `JalaliDateParseError`; they are not modeled with nullable strings, ambiguous booleans, or exceptions. Constructor invariant violations still fail fast because they indicate programmer misuse. Clock-dependent behavior accepts `java.time.Clock` for deterministic tests.

State mutation compatibility methods remain `Unit`-returning, while new `tryDispatch`, `trySelect`, and `tryReplace` methods expose explicit state-transition outcomes. This preserves source compatibility while giving new consumers a type-safe validation path.

## Platform decision

The repository currently has real Android and JVM targets only. It is not labeled KMP and does not declare inactive iOS, Desktop, JS, or Wasm targets. `calendar-core` keeps framework-free logic behind a clean boundary so a future KMP migration can be evidence-driven rather than a cosmetic Gradle conversion.

## Release architecture

The release boundary includes more than Maven upload:

1. static repository policy and license-hash checks;
2. Core, Android, format, lint, and independent-consumer verification;
3. aggregate CycloneDX SBOM generation;
4. Maven-local artifact collection and checksum validation;
5. signed Maven Central publication;
6. GitHub provenance and SBOM attestations;
7. immutable release evidence with source commit/ref metadata attached to the tag.

See `docs/adr/0004-release-evidence-and-supply-chain.md`.
