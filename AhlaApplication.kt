package com.gaber.ahlamenelasal

import android.app.Application
import com.cloudinary.android.MediaManager

class AhlaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // إعداد Cloudinary
        // ملاحظة: ستحتاج لاستبدال هذه القيم ببيانات حسابك الحقيقية من Cloudinary Dashboard
        val config = mapOf(
            "cloud_name" to "dfvlkpuxv", // استبدل بـ cloud_name الخاص بك
            "api_key" to "362371948834167",       // استبدل بـ api_key الخاص بك
            "api_secret" to "M_v8y8Sj6Y6X6x6X6X6X6X6X6X6"    // استبدل بـ api_secret الخاص بك
        )
        
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // التحقق مما إذا كان قد تم تهيئته بالفعل (لتجنب الخطأ عند إعادة التشغيل)
        }
    }
}
