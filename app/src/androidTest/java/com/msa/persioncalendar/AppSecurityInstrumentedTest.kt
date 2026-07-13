package com.msa.persioncalendar

import android.content.pm.ApplicationInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSecurityInstrumentedTest {

    @Test
    fun sampleAppPackageAndBackupFlagMatchProductionSafeManifest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)

        assertEquals("com.msa.persioncalendar", context.packageName)
        assertFalse(applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP != 0)
    }
}
