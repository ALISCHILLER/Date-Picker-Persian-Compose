# Persian Calendar Compose

![Build verification required](https://img.shields.io/badge/build-verification_required-orange)

یک کتابخانه انتخاب تاریخ جلالی برای Android است که با Jetpack Compose و Material 3 ساخته شده است. مخزن شامل ماژول قابل‌استفاده‌مجدد `:calendar` و برنامه نمونه `:app` است. پروژه **Android-only** است و Compose Multiplatform/KMM نیست.

## قابلیت‌ها

- انتخاب یک تاریخ یا بازه تاریخ با خروجی‌های strongly typed.
- تبدیل قطعی تاریخ جلالی و میلادی بدون وابستگی به ICU یا APIهای Android.
- نمایش خودکار ماه میلادی متناظر با ماه شمسی، از جمله بازه‌های دوماهه و عبور از سال میلادی.
- پشتیبانی از فارسی و انگلیسی، ارقام فارسی و لاتین، RTL و LTR.
- حداقل/حداکثر تاریخ، تاریخ‌های غیرفعال، validator سفارشی و حداکثر طول بازه.
- جلوگیری از عبور بازه از تاریخ‌های غیرقابل انتخاب به‌صورت پیش‌فرض.
- Quick action، نمایش رویداد، تم روشن/تیره و Dynamic Color اختیاری.
- حفظ وضعیت انتخاب و صفحه جاری در recreation با `rememberSaveable`.
- ناوبری ایمن در محدوده سال‌های موتور تقویم و محدوده تعیین‌شده توسط مصرف‌کننده.
- Semantics مناسب برای TalkBack و کنترل‌های ناوبری با touch target استاندارد.

## ساختار پروژه

- `:calendar` — کتابخانه اصلی، مدل‌ها، موتور تقویم، تنظیمات و UIهای Compose.
- `:app` — Showcase برای تست دستی Single Picker و Range Picker.

## نسخه‌های اصلی

- Kotlin `2.2.20`
- Android Gradle Plugin `8.10.1`
- Gradle Wrapper `8.11.1`
- Compose BOM `2026.06.00`
- Compile SDK `36`
- Target SDK `35`
- Min SDK `26`
- Java `17`

تمام نسخه‌ها در `gradle/libs.versions.toml` متمرکز شده‌اند.

## اجرای سریع

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

روی Windows:

```powershell
.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug
```

راهنمای کامل محیط توسعه، release، signing و رفع خطاها در [SETUP.md](SETUP.md) قرار دارد.

## نمونه استفاده

```kotlin
if (showPicker) {
    PersianDatePickerDialog(
        initialDate = SoleimaniDate(1405, 4, 21),
        config = DatePickerConfig(
            yearRange = 1380..1450,
            showGregorianDateHints = true,
            enableHaptics = true,
            motionSpec = DatePickerMotionSpec(enabled = true),
            constraints = DatePickerConstraints(
                minDate = SoleimaniDate(1400, 1, 1),
                maxDate = SoleimaniDate(1450, 12, 29),
            ),
        ),
        onClose = { showPicker = false },
        onSelectionConfirmed = { selection ->
            showPicker = false
            // selection.date, selection.gregorianDate, selection.formattedDate
        },
    )
}
```

نام صحیح API تقویم `PersianCalendar` است. نام قدیمی `PersionCalendar` برای سازگاری پروژه‌های قبلی باقی مانده است.

## اعتبارسنجی بازه

`DatePickerConstraints` به‌صورت پیش‌فرض همه روزهای داخل بازه را بررسی می‌کند؛ بنابراین بازه نمی‌تواند از یک روز غیرفعال یا روز ردشده توسط `dateValidator` عبور کند. برای بازگرداندن رفتار قدیمی فقط-دو-سر-بازه:

```kotlin
DatePickerConstraints(
    rangeValidationMode = RangeValidationMode.EndpointsOnly,
)
```

## طراحی واقعاً Responsive در V8

- اندازه Dialog از `BoxWithConstraints` و فضای واقعی باقی‌مانده پس از system bar، display cutout و IME محاسبه می‌شود؛ دیگر به ابعاد کلی دستگاه در `LocalConfiguration` وابسته نیست.
- این رفتار در Split Screen، Freeform Window، گوشی تاشو، Landscape و پنجره‌های embed‌شده نیز براساس constraint واقعی همان لحظه تصمیم می‌گیرد.
- روی موبایل تقریباً تمام عرض امن مصرف می‌شود؛ عرض محتوای تبلت برای خوانایی حداکثر `560dp` است و خانه‌های روز بیش از `58dp` کشیده نمی‌شوند.
- Range Header سه حالت دارد: `Inline` برای فضای عادی، `Stacked` برای عرض باریک یا فونت بزرگ، و `Condensed` برای پنجره کوتاه و Landscape.
- در حالت Condensed تاریخ شروع و پایان همچنان کامل دیده می‌شوند، اما عنوان‌ها و جزئیات تکراری حذف می‌شوند تا Grid تقویم فشرده نشود.
- Header روزهای هفته و Grid از یک تابع محاسبه عرض مشترک استفاده می‌کنند؛ بنابراین در موبایل و تبلت دقیقاً روی یک ستون قرار می‌گیرند.
- Grid روزها فضای باقی‌مانده را می‌گیرد و در ارتفاع کوتاه، اندازه روزها را کوچک نمی‌کند؛ فقط همان ناحیه به‌صورت کنترل‌شده اسکرول می‌شود.
- انتخاب ماه بین ۲، ۳ و ۴ ستون تغییر می‌کند و انتخاب سال از ستون‌های Adaptive استفاده می‌کند.
- Action Bar در عرض بسیار باریک یا Font Scale بالا عمودی می‌شود و متن دکمه‌ها بریده نمی‌شود.
- مقدار عددی تاریخ با جهت LTR رندر می‌شود تا ترتیب `YYYY/MM/DD` در محیط RTL به‌هم نریزد.
- Header ماه، معادل دقیق میلادی را براساس روز اول و آخر همان ماه نشان می‌دهد؛ مثلاً `فروردین ۱۴۰۴` به‌صورت `مارس – آوریل ۲۰۲۵`.
- نمایش معادل میلادی با `showGregorianDateHints = false` قابل غیرفعال‌کردن است.

## طراحی Ultra Polished و بهینه‌سازی V8

- پالت حرفه‌ای Royal Blue/Teal/Neutral Slate با رنگ‌های مستقل و کنتراست کنترل‌شده برای Light و Dark.
- انتخاب روز با گرادیان سبک و Range پیوسته؛ تاریخ‌های بین شروع و پایان دیگر مانند کارت‌های جدا دیده نمی‌شوند.
- Grid روزها یک ساختار ثابت ۷×۶ دارد. برای فقط ۴۲ خانه از lazy layout استفاده نمی‌شود و هزینه اندازه‌گیری، key management و scroll bookkeeping حذف شده است.
- اطلاعات انتخاب‌پذیری و eventهای ماه یک‌بار در `MonthRenderSnapshot` محاسبه و با آرایه مستقیم روز ۱ تا ۳۱ خوانده می‌شوند؛ Map و hash lookup از مسیر رندر حذف شده است.
- Shapeها و Brushهای پرتکرار cache می‌شوند و پس‌زمینه‌های تزئینی با `drawWithCache` رسم می‌شوند.
- animation، shadow، `graphicsLayer` و press-state جداگانه از تک‌تک سلول‌های روز حذف شده‌اند؛ ripple استاندارد Material حفظ شده است.
- در حالت عادی Grid بدون scroll container اضافی رندر می‌شود و فقط در ارتفاع کوتاه یا Landscape اسکرول فعال می‌شود.
- انتخاب ماه و سال، quick actionها، Header بازه و دکمه‌های پایین با یک زبان بصری واحد بازطراحی شده‌اند.
- تمام گزینه‌های سفارشی‌سازی قبلی حفظ شده‌اند و رنگ‌های جدید با `DatePickerColors` قابل override هستند.
- سلول روز و مدل تاریخ با `@Immutable` علامت‌گذاری شده‌اند تا Compose بتواند پایداری پارامترها را بهتر تشخیص دهد.
- صفحه انتخاب ۱۲ ماه از Grid ثابت سبک استفاده می‌کند؛ Lazy layout فقط برای فهرست بزرگ سال‌ها باقی مانده است.
- روی تبلت اندازه هر خانه روز سقف `58dp` دارد و Grid در مرکز می‌ماند تا تقویم بیش از حد کشیده نشود.
- Action Bar در عرض باریک یا Font Scale بزرگ خودکار عمودی می‌شود و متن دکمه‌ها کوچک یا بریده نمی‌شود.
- Motion فقط در سطح صفحه/ماه اجرا می‌شود و از `DatePickerMotionSpec` قابل کنترل یا خاموش‌کردن است؛ سلول‌های روز بدون animation مستقل باقی می‌مانند.
- Haptic feedback انتخاب روز با `enableHaptics` قابل کنترل است.
- خطاهای انتخاب بازه در Banner درون‌خطی با live-region مناسب TalkBack نمایش داده می‌شوند.

این تغییرات کاهش هزینه ساخت UI را از نظر ساختاری تضمین می‌کنند، اما هیچ درصدی برای بهبود frame time بدون Macrobenchmark روی دستگاه واقعی ادعا نمی‌شود.

## تست و CI

- Unit testهای موتور تقویم، تبدیل رفت‌وبرگشت، constraints، formatting و state helperها.
- Instrumented testهای resource و تنظیمات امنیتی برنامه نمونه.
- Compose UI test برای مرز ناوبری ماه‌ها، دیده‌شدن تاریخ‌های لاتین/فارسی و نمایش معادل‌های میلادی در Header.
- Workflow آماده GitHub Actions برای Unit Test، Lint، Debug/Release Build و Managed Device tests در `.github/workflows/android.yml`.
- Baseline Profile اولیه در هر دو ماژول؛ برای release واقعی باید با Macrobenchmark روی دستگاه نماینده بازتولید و اندازه‌گیری شود.

جزئیات تصمیم‌های معماری در [ARCHITECTURE.md](ARCHITECTURE.md) آمده است.

## وضعیت Build

Workflow پروژه Unit Test، Lint، Debug Build، minified Release Build و instrumented testهای Managed Device را اجرا می‌کند. در محیطی که Gradle distribution، Google Maven و Maven Central در دسترس نباشند، build کامل قابل تأیید نیست؛ در این حالت خروجی release را منتشر نکنید تا CI یا سیستم محلی سبز شود.

## License

این پروژه تحت **GNU Affero General Public License v3.0 (AGPL-3.0)** منتشر می‌شود. متن کامل در [LICENSE.md](LICENSE.md) قرار دارد.

AGPL استفاده، تغییر و استفاده تجاری را مجاز می‌داند، اما در صورت توزیع نرم‌افزار یا ارائه آن از طریق شبکه، تعهدات ارائه source code متناظر و حفظ همین مجوز باید رعایت شوند.
