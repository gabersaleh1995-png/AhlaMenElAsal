package com.gaber.ahlamenelasal.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "الرئيسية")
    object Chat : Screen("chat", "الدردشة")
    object Meetings : Screen("meetings", "المواعيد")
    object Videos : Screen("videos", "الفيديوهات")
    object Library : Screen("library", "المكتبة PDF")
    object AudioLibrary : Screen("audio_library", "تسجيلات صوتية")
    object Gallery : Screen("gallery", "معرض الصور")
    object Topics : Screen("topics", "الموضوعات")
    
    // غرف الدردشة
    object GroupChat : Screen("group_chat", "دردشة الجماعة")
    object AdminChat : Screen("admin_chat/{userId}/{userName}", "دردشة") {
        fun createRoute(userId: String, userName: String = "دردشة") = "admin_chat/$userId/$userName"
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
