package com.gaber.ahlamenelasal.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "الرئيسية")
    object Bible : Screen("bible", "الكتاب المقدس")
    object Chat : Screen("chat", "الدردشة")
    object Meetings : Screen("meetings", "المواعيد")
    object Videos : Screen("videos", "الفيديوهات")
    object Commentary : Screen("commentary", "التفسير")
    object Library : Screen("library", "المكتبة PDF")
    object AudioLibrary : Screen("audio_library", "تسجيلات صوتية")
    
    // مسارات فرعية للكتاب المقدس
    object BibleChapters : Screen("bible_chapters/{bookName}/{chapterCount}", "الأصحاحات") {
        fun createRoute(bookName: String, chapterCount: Int) = "bible_chapters/$bookName/$chapterCount"
    }
    object BibleVerses : Screen("bible_verses/{bookName}/{chapterNumber}", "الآيات") {
        fun createRoute(bookName: String, chapterNumber: Int) = "bible_verses/$bookName/$chapterNumber"
    }

    // غرف الدردشة
    object GroupChat : Screen("group_chat", "دردشة الجماعة")
    object AdminChat : Screen("admin_chat/{userId}/{userName}", "دردشة") {
        fun createRoute(userId: String, userName: String = "دردشة") = "admin_chat/$userId/$userName"
    }

    // الأجبية
    object Agbeya : Screen("agbeya", "الأجبية")
    object AgbeyaContent : Screen("agbeya_content/{prayerName}", "نص الصلاة") {
        fun createRoute(prayerName: String) = "agbeya_content/$prayerName"
    }

    // سؤال الأسبوع
    object WeeklyQuestion : Screen("weekly_question", "سؤال الأسبوع")
    object AdminAnswers : Screen("admin_answers/{questionId}", "إجابات الأعضاء") {
        fun createRoute(questionId: String) = "admin_answers/$questionId"
    }

    // لوحة تحكم الأدمن
    object Admin : Screen("admin", "لوحة التحكم")
    object AdminPrivateChats : Screen("admin_private_chats", "الرسائل الخاصة")

    // الإعدادات
    object Settings : Screen("settings", "الإعدادات")

    // المصادقة
    object Login : Screen("login", "تسجيل الدخول")
    object SignUp : Screen("signup", "إنشاء حساب")
}
