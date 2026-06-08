package com.gaber.ahlamenelasal

import android.app.Application
import com.cloudinary.android.MediaManager
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class AhlaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // إعداد OneSignal
        // Verbose Logging helps determine if OneSignal is setup correctly
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, "db34f426-32ac-4093-8578-d76f80780237")
        
        // إعداد Cloudinary بناءً على صورتك رقم 45
        val config = mapOf(
            "cloud_name" to "daf4gj3p9",
            "secure" to true
        )
        
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // تم التهيئة مسبقاً
        }
    }
}
