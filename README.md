<div align="center">

# 🍯 أحلى من العسل
### *AhlaMenElAsal*

تطبيق أندرويد مجتمعي ديني متكامل مبني بأحدث تقنيات Jetpack Compose

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0.21-purple?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-Private-red)](LICENSE)

</div>

---

## 📋 نظرة عامة

**أحلى من العسل** هو تطبيق مجتمعي ديني متكامل يتيح للأعضاء التواصل، متابعة المحتوى الديني، قراءة الكتاب المقدس والأجبية، والمشاركة في الاجتماعات والأنشطة. يدعم التطبيق نظام إدارة كامل للأدمن مع إشعارات فورية لجميع المستخدمين.

---

## ✨ المميزات الرئيسية

### 📖 المحتوى الديني
| الميزة | الوصف |
|--------|--------|
| 📖 الكتاب المقدس | تصفح كامل للعهد القديم والجديد (٦٦ سفراً) مع تفسير الآيات |
| 🙏 صلوات الأجبية | ٨ صلوات يومية كاملة مع النصوص |
| ✨ آية اليوم | آية يومية يحددها الأدمن مع إشعار فوري |
| ❓ سؤال الأسبوع | سؤال أسبوعي مع إمكانية إرسال وتعديل الإجابة |

### 🎬 المكتبة الرقمية
| الميزة | الوصف |
|--------|--------|
| 📚 مكتبة PDF | كتب ومراجع منظّمة في مجلدات وأقسام |
| 🎙️ تسجيلات صوتية | مشغّل صوتي مدمج مع شريط تقدم وإمكانية التحميل |
| 🎬 مكتبة الفيديو | دعم فيديوهات داخلية (Cloudinary) وخارجية (YouTube/Drive) |
| 🖼️ معرض الصور | عرض الصور بشبكة مع إمكانية التكبير والتحميل |
| 🗂️ الموضوعات | موضوعات مصورة وفيديو مع وصف تفصيلي |

### 💬 التواصل
| الميزة | الوصف |
|--------|--------|
| 👥 دردشة جماعية | غرفة نقاش مفتوحة لجميع الأعضاء مع إمكانية القفل بكود |
| 🛡️ دردشة خاصة | تواصل مباشر مع الأدمن |
| 📅 المواعيد | جدولة الاجتماعات مع إشعار تذكيري تلقائي |
| 🔔 إشعارات فورية | OneSignal لإرسال إشعارات فورية لجميع المستخدمين |

### ⚙️ لوحة تحكم الأدمن
- رفع المحتوى (صور، فيديوهات، PDFs، صوتيات) عبر Cloudinary
- إدارة الأعضاء (عرض وحذف)
- التحكم في غرف الدردشة (فتح/قفل بكود)
- إرسال إشعارات مخصصة للجميع
- إضافة وحذف الاجتماعات والمواعيد
- نشر سؤال الأسبوع ومراجعة الإجابات

---

## 🏗️ بنية المشروع

```
AhlaMenElAsal/
├── app/src/main/java/com/gaber/ahlamenelasal/
│   ├── AhlaApplication.kt          # تهيئة التطبيق
│   ├── MainActivity.kt             # النشاط الرئيسي
│   │
│   ├── data/
│   │   ├── model/
│   │   │   ├── ChatMessage.kt      # نموذج رسالة الدردشة
│   │   │   ├── Meeting.kt          # نموذج الاجتماع
│   │   │   ├── Topic.kt            # نموذج الموضوع
│   │   │   └── VideoItem.kt        # نموذج الفيديو
│   │   ├── AgbeyaData.kt           # بيانات صلوات الأجبية
│   │   ├── BibleData.kt            # بيانات الكتاب المقدس
│   │   └── BibleCommentaryData.kt  # التفاسير
│   │
│   ├── navigation/
│   │   └── Screen.kt               # تعريف شاشات التنقل
│   │
│   ├── service/
│   │   └── MyFirebaseMessagingService.kt  # استقبال إشعارات FCM
│   │
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── HomeScreen.kt           # الشاشة الرئيسية
│   │   │   ├── LoginScreen.kt          # تسجيل الدخول
│   │   │   ├── SignUpScreen.kt          # إنشاء حساب
│   │   │   ├── BibleScreen.kt          # الكتاب المقدس
│   │   │   ├── ChaptersScreen.kt       # أصحاحات السفر
│   │   │   ├── VersesScreen.kt         # آيات الأصحاح
│   │   │   ├── CommentaryScreen.kt     # التفسير
│   │   │   ├── AgbeyaScreen.kt         # الأجبية
│   │   │   ├── AgbeyaContentScreen.kt  # محتوى الصلاة
│   │   │   ├── TopicsScreen.kt         # الموضوعات
│   │   │   ├── AudioLibraryScreen.kt   # مكتبة الصوتيات
│   │   │   ├── VideosScreen.kt         # مكتبة الفيديو
│   │   │   ├── GalleryScreen.kt        # معرض الصور
│   │   │   ├── LibraryScreen.kt        # مكتبة PDF
│   │   │   ├── MeetingsScreen.kt       # المواعيد
│   │   │   ├── WeeklyQuestionScreen.kt # سؤال الأسبوع
│   │   │   ├── ChatSelectionScreen.kt  # اختيار غرفة الدردشة
│   │   │   ├── ChatScreen.kt           # الدردشة
│   │   │   ├── SettingsScreen.kt       # الإعدادات
│   │   │   ├── AdminScreen.kt          # لوحة تحكم الأدمن
│   │   │   ├── AdminAnswersScreen.kt   # إجابات سؤال الأسبوع
│   │   │   └── AdminPrivateChatsScreen.kt # الرسائل الخاصة
│   │   │
│   │   ├── theme/
│   │   │   ├── Color.kt            # لوحة الألوان (Honey Gold + Deep Purple)
│   │   │   ├── Theme.kt            # تهيئة الثيم (فاتح/داكن)
│   │   │   └── Type.kt             # إعدادات الخط
│   │   │
│   │   └── viewmodel/
│   │       ├── AdminViewModel.kt   # منطق لوحة الأدمن
│   │       ├── AuthViewModel.kt    # منطق المصادقة
│   │       ├── BibleViewModel.kt   # منطق الكتاب المقدس
│   │       ├── ChatViewModel.kt    # منطق الدردشة
│   │       ├── MeetingsViewModel.kt
│   │       ├── SettingsViewModel.kt
│   │       ├── TopicsViewModel.kt
│   │       └── VideosViewModel.kt
│   │
│   └── util/
│       └── NotificationHelper.kt  # إرسال إشعارات OneSignal
│
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # إدارة إصدارات المكتبات
└── README.md
```

---

## 🛠️ التقنيات المستخدمة

### واجهة المستخدم
| المكتبة | الإصدار | الاستخدام |
|---------|---------|-----------|
| Jetpack Compose BOM | 2024.09.00 | واجهة المستخدم التفاعلية |
| Material 3 | - | مكونات التصميم |
| Navigation Compose | 2.9.7 | التنقل بين الشاشات |
| Coil | 2.7.0 | تحميل وعرض الصور |
| Media3 ExoPlayer | 1.5.1 | تشغيل الفيديو |

### الخلفية والبنية التحتية
| الخدمة | الاستخدام |
|--------|-----------|
| Firebase Auth | تسجيل الدخول (بريد إلكتروني + Google) |
| Firebase Firestore | قاعدة البيانات الرئيسية |
| Firebase Storage | تخزين الملفات |
| Firebase Messaging (FCM) | استقبال الإشعارات |
| Firebase Analytics | تحليل استخدام التطبيق |
| Cloudinary | رفع واستضافة الصور والفيديوهات والصوتيات |
| OneSignal 5.x | إرسال إشعارات Push للمستخدمين |
| Google Sign-In | تسجيل الدخول بجوجل |
| OkHttp | طلبات HTTP لـ OneSignal API |

### اللغة والأدوات
| الأداة | الإصدار |
|--------|---------|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 9.0.1 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## 🚀 كيفية تشغيل المشروع

### المتطلبات الأساسية
- Android Studio Hedgehog أو أحدث
- JDK 17
- حساب Firebase
- حساب Cloudinary
- حساب OneSignal

### خطوات الإعداد

**١. استنساخ المشروع**
```bash
git clone https://github.com/gabersaleh1995-png/AhlaMenElAsal.git
cd AhlaMenElAsal
```

**٢. إعداد Firebase**
- أنشئ مشروعاً جديداً على [Firebase Console](https://console.firebase.google.com)
- أضف تطبيق Android بـ package name: `com.gaber.ahlamenelasal`
- حمّل ملف `google-services.json` وضعه في مجلد `app/`
- فعّل الخدمات التالية:
  - Authentication (Email/Password + Google)
  - Firestore Database
  - Storage
  - Cloud Messaging

**٣. إعداد Cloudinary**

في ملف `AhlaApplication.kt` أو `AdminViewModel.kt`، تأكد من وجود:
```kotlin
MediaManager.init(context, mapOf(
    "cloud_name" to "YOUR_CLOUD_NAME",
    "api_key"    to "YOUR_API_KEY",
    "api_secret" to "YOUR_API_SECRET"
))
```

**٤. إعداد OneSignal**

في ملف `NotificationHelper.kt`:
```kotlin
private const val ONESIGNAL_APP_ID  = "your-onesignal-app-id"
private const val ONESIGNAL_API_KEY = "your-rest-api-key"
```

في [OneSignal Dashboard](https://app.onesignal.com):
- تأكد من وجود Segment باسم **`All`**
- أضف FCM Server Key من Firebase Console

**٥. تشغيل التطبيق**
```bash
# فتح المشروع في Android Studio
# ثم اضغط Run ▶️ أو Shift + F10
```

---

## 🔥 إعداد Firestore

### هيكل قاعدة البيانات

```
Firestore
├── users/{userId}
│   ├── name: String
│   ├── email: String
│   └── isAdmin: Boolean
│
├── app_data/daily_verse
│   ├── text: String
│   └── reference: String
│
├── topics/{topicId}
│   ├── title: String
│   ├── description: String
│   ├── mediaUrl: String
│   ├── mediaType: "IMAGE" | "VIDEO"
│   └── timestamp: Timestamp
│
├── questions/{questionId}
│   ├── text: String
│   ├── timestamp: Timestamp
│   └── answers/{answerId}
│       ├── userId: String
│       ├── userName: String
│       ├── text: String
│       └── timestamp: Timestamp
│
├── meetings/{meetingId}
│   ├── title: String
│   ├── date: String
│   ├── time: String
│   ├── location: String
│   ├── description: String
│   └── timestamp: Timestamp
│
├── images/{imageId}
│   ├── title: String
│   ├── url: String
│   └── timestamp: Timestamp
│
├── videos/{videoId}
│   ├── title: String
│   ├── url: String
│   ├── category: String
│   └── timestamp: Timestamp
│
├── audios/{audioId}
│   ├── title: String
│   ├── url: String
│   └── timestamp: Timestamp
│
├── bible_pdfs/{pdfId}
│   ├── title: String
│   ├── url: String
│   ├── folder: String
│   ├── subFolder: String
│   └── timestamp: Timestamp
│
├── chats/group_all/messages/{msgId}
│   ├── senderId: String
│   ├── senderName: String
│   ├── text: String
│   └── timestamp: Timestamp
│
└── chat_settings/group_all
    ├── status: "open" | "locked"
    └── passcode: String
```

### Firestore Security Rules
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // المستخدمون: يقرأ الكل، يكتب صاحب الحساب أو الأدمن
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId
                   || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
    }

    // المحتوى العام: يقرأ الكل المسجّل، يكتب الأدمن فقط
    match /{collection}/{docId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
                   && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
    }

    // الدردشة: يقرأ ويكتب المسجّلون
    match /chats/{chatId}/messages/{msgId} {
      allow read, write: if request.auth != null;
    }

    // إجابات الأسئلة: يكتب صاحب الإجابة فقط
    match /questions/{qId}/answers/{aId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null;
    }
  }
}
```

---

## 🔔 نظام الإشعارات

### كيف يعمل
```
الأدمن ينشر محتوى
       ↓
AdminViewModel → saveInfoToFirestore()
       ↓
NotificationHelper.notifyAll()
       ↓
OneSignal REST API → Segment "All"
       ↓
جميع الأجهزة المسجّلة تستقبل الإشعار
```

### أنواع الإشعارات
| الحدث | عنوان الإشعار |
|-------|--------------|
| آية اليوم | آية اليوم الجديدة ✨ |
| موضوع جديد | موضوع جديد: [العنوان] 🍯 |
| PDF جديد | كتاب PDF جديد 📚 |
| فيديو/صوت | محتوى جديد 🍯 |
| سؤال الأسبوع | سؤال الأسبوع الجديد ❓ |
| اجتماع | تذكير بموعد اجتماع 📅 |
| إشعار مخصص | حسب اختيار الأدمن |

---

## 🎨 نظام التصميم

### لوحة الألوان
```kotlin
// الألوان الأساسية
HoneyGold    = #F5C518   // ذهبي عسلي — اللون المميز
HoneyAmber   = #E8900A   // عنبري دافئ
DeepPurple   = #1A0A2E   // بنفسجي عميق — الخلفية الداكنة
MidPurple    = #7C3AED   // بنفسجي متوسط — اللون الأساسي
FuchsiaAccent = #C850C0  // فيوشيا — لون تكميلي
```

### الخطوط
- **Tajawal** — للعناوين الكبيرة
- **Cairo** — لباقي النصوص

### الأوضاع
- ☀️ وضع فاتح (Light Mode)
- 🌙 وضع داكن (Dark Mode)
- 📱 دعم وضع النظام التلقائي

---

## 📱 الصلاحيات المطلوبة

| الصلاحية | السبب |
|----------|-------|
| `INTERNET` | الاتصال بالخوادم |
| `ACCESS_NETWORK_STATE` | فحص الاتصال |
| `POST_NOTIFICATIONS` | استقبال الإشعارات |
| `RECORD_AUDIO` | تسجيل الصوت (مستقبلاً) |
| `RECEIVE_BOOT_COMPLETED` | جدولة الإشعارات بعد إعادة التشغيل |
| `WAKE_LOCK` | ضمان وصول الإشعارات |
| `WRITE_EXTERNAL_STORAGE` | تحميل الملفات (Android ≤ 12) |

---

## 🗂️ ملفات مهمة يجب تجاهلها في Git

```gitignore
# ملفات حساسة — لا ترفعها أبداً
google-services.json
local.properties
*.keystore
*.jks

# Android Studio
.idea/
*.iml
build/
.gradle/
```

---

## 🤝 المساهمة في المشروع

1. افتح Issue لتوضيح التغيير المقترح
2. أنشئ Branch جديداً: `git checkout -b feature/اسم-الميزة`
3. اعمل Commit: `git commit -m "feat: وصف التغيير"`
4. ارفع: `git push origin feature/اسم-الميزة`
5. افتح Pull Request

---

## 📄 الترخيص

هذا المشروع خاص ومحمي. جميع الحقوق محفوظة © 2024 Gaber Saleh.

---

<div align="center">

صُنع بـ ❤️ و ☕ في مصر 🇪🇬

**[⭐ أعطنا نجمة على GitHub](https://github.com/gabersaleh1995-png/AhlaMenElAsal)**

</div>
