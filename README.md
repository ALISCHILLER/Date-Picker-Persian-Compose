
#Title:
# Date Picker Persian Compose 
`Date Picker Persian Compose`: a Jetpack Compose library for date and time selection in Farsi language 


**Summary:**
 DatePickers Persian Compose is a Jetpack Compose library that allows you to choose date and time in Farsi language. This library uses a simple and intuitive user interface that makes it easy.
```
DatePicker Persian android  Jetpack Compose 
```
## Theme
<img src="https://github.com/ALISCHILLER/DatePicker-Persian-Compose/blob/main/media/Screen_Recording_20230919_162830_PersionCalendar.gif" width="400" height="796px">&emsp;
<img src="https://github.com/ALISCHILLER/DatePicker-Persian-Compose/blob/main/media/Screenshot_20230919_162724_PersionCalendar.jpg" width="400" height="596px"/>

**Attributes:**

* Support for selecting the date and time in Farsi language
* Simple and intuitive user interface
* Ability to customize

**How to use:**

To use DatePickers Persian Compose, you must first add it to your Jetpack Compose project. You can do this by adding the following substring to your build.gradle file:

```

All projects {
reservoirs
...
maven { url 'https://www.jitpack.io' }
}
}
```
```

Dependencies {
implementation 'com.github.ALISCHILLER:Date-Picker-Persian-Compose:0.0.2'
}
```

After installing the library, you can use it in your project. To do this, you need to add the `CalendarScreen` component to your Jetpack Compose file:

Kathleen
import com.github.ALISCHILLER.date_pickers_persian.DatePicker

@Composable
fun program () {
   calendar page (
   onDismiss = { hideDatePicker = true },
    onConfirm = { setDate = it }
     )
}
```


import com.github.ALISCHILLER.date_pickers_persian.DatePicker

@Composable
fun program () {
    calendar page (
     onDismiss = { hideDatePicker = true },
     onConfirm = { setDate = it }
   )
}
```

This code creates a simple `DatePicker` component and saves the selected date to a `date` variable.


  The GNU General Public License is a free, copyleft license for
software and other kinds of works.

**connections:**

For more information, please visit the GitHub page: https://github.com/ALISCHILLER/DatePicker-Persian-Compose.

**Version:** 0.0.1

**Date:** 2023-09-19

**Author:** Ali Soleimani

**Changes:**

* Version 0.0.1: Initial release

**Implementation:**


An onSelect event is called when the user selects a date or time. This event receives the date or selected value as a time.



**Development:**

The DatePicker Persian Compose library is under development. In the future, new features support language development and support selection



**License:**

                    GNU GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

                  
