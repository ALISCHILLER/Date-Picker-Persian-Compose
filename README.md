**عنوان:** DatePickersPersion: یک کتابخانه Jetpack Compose برای انتخاب تاریخ و زمان به زبان فارسی

**خلاصه:**

DatePickersPersion یک کتابخانه Jetpack Compose است که به شما امکان می دهد تاریخ و زمان را به زبان فارسی انتخاب کنید. این کتابخانه از یک رابط کاربری ساده و بصری استفاده می کند که استفاده از آن را آسان می کند.

**ویژگی ها:**

* پشتیبانی از انتخاب تاریخ و زمان به زبان فارسی
* رابط کاربری ساده و بصری
* قابلیت سفارشی سازی

**نحوه استفاده:**

برای استفاده از DatePickersPersion، ابتدا باید آن را به پروژه Jetpack Compose خود اضافه کنید. می توانید این کار را با افزودن زیرمجموعه زیر به فایل build.gradle خود انجام دهید:

```

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
```

dependencies {
	        implementation 'com.github.ALISCHILLER:DatePickersPersion:Tag'
	}
```

پس از نصب کتابخانه، می توانید از آن در پروژه خود استفاده کنید. برای این کار، باید کامپوننت `CalendarScreen` را به فایل Jetpack Compose خود وارد کنید:

```kotlin
import com.github.ALISCHILLER.date_pickers_persian.DatePicker

@Composable
fun App() {
 CalendarScreen(
 onDismiss = { hideDatePicker = true },
  onConfirm = { setDate = it }
   )
}
```

این کد یک کامپوننت `DatePicker` ساده ایجاد می کند که یک تاریخ را به زبان فارسی نمایش می دهد.

برای سفارشی سازی کامپوننت `DatePicker`، می توانید از ویژگی های زیر استفاده کنید:



**مثال:**



این کد یک کامپوننت `DatePicker` را ایجاد می کند که یک تاریخ را به زبان فارسی نمایش می دهد و تاریخ پیش فرض امروز است. همچنین، یک تابع `onSelect` را تعریف می کند که هنگام انتخاب تاریخ فراخوانی می شود و تاریخ انتخاب شده را به کنسول چاپ می کند.

**آزمایش:**

برای آزمایش کتابخانه DatePickersPersion، می توانید از مثال زیر استفاده کنید:

```kotlin
import com.github.ALISCHILLER.date_pickers_persian.DatePicker

@Composable
fun App() {
  CalendarScreen(
   onDismiss = { hideDatePicker = true },
   onConfirm = { setDate = it }
 )
}
```

این کد یک کامپوننت `DatePicker` ساده ایجاد می کند و تاریخ انتخاب شده را به یک متغیر `date` ذخیره می کند.

**مجوز:**

DatePickersPersion تحت مجوز MIT منتشر می شود.

**ارتباطات:**

برای اطلاعات بیشتر، لطفا به صفحه GitHub: https://github.com/ALISCHILLER/DatePickersPersion مراجعه کنید.

**نسخه:** 0.0.1

**تاریخ:** 2023-09-19

**نویسنده:** Ali Schiller

**تغییرات:**

* نسخه 0.0.1: انتشار اولیه

**پیاده سازی:**


هنگامی که کاربر تاریخ یا زمان را انتخاب می کند، یک رویداد `onSelect` فراخوانی می شود. این رویداد مقدار تاریخ یا زمان انتخاب شده را به عنوان یک پارامتر دریافت می کند.



**توسعه:**

کتابخانه DatePickersPersion در حال توسعه است. در آینده، ویژگی های جدیدی مانند پشتیبانی از چندین زبان و پشتیبانی از انتخاب
