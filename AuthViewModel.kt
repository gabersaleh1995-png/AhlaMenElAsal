
package com.gaber.ahlamenelasal.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var currentUser = mutableStateOf(auth.currentUser)
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var isAdmin = mutableStateOf(false)

    init {
        // فحص حالة المشرف فور تشغيل الـ ViewModel إذا كان هناك مستخدم مسجل
        if (auth.currentUser != null) {
            checkAdminStatus {}
        }
    }

    fun checkAdminStatus(onComplete: () -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // استخدام getBoolean مع تأكيد الاسم كما هو في قاعدة البيانات
                        val adminValue = doc.getBoolean("isAdmin") ?: false
                        isAdmin.value = adminValue
                        Log.d("AuthDebug", "تم التحقق من صلاحيات الأدمن: $adminValue")
                    } else {
                        isAdmin.value = false
                        Log.d("AuthDebug", "المستند غير موجود في Firestore")
                    }
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e("AuthDebug", "خطأ في الوصول لـ Firestore: ${e.message}")
                    isAdmin.value = false
                    onComplete()
                }
        } else {
            isAdmin.value = false
            onComplete()
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage.value = "يرجى ملء جميع الحقول"
            return
        }
        isLoading.value = true
        errorMessage.value = null // تصغير الأخطاء السابقة

        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser.value = auth.currentUser
                    checkAdminStatus {
                        isLoading.value = false
                        onSuccess()
                    }
                } else {
                    isLoading.value = false
                    errorMessage.value = "خطأ في الدخول: ${task.exception?.localizedMessage}"
                }
            }
    }

    fun signUp(name: String, email: String, pass: String, adminCode: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            errorMessage.value = "يرجى ملء جميع الحقول"
            return
        }
        isLoading.value = true
        errorMessage.value = null

        // تنظيف الكود من أي مسافات زائدة لضمان دقة التحقق
        val shouldBeAdmin = adminCode.trim() == "ga1234#"

        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""

                    val userUpdates = hashMapOf(
                        "uid" to uid,
                        "name" to name.trim(),
                        "email" to email.trim(),
                        "isAdmin" to shouldBeAdmin
                    )

                    // حفظ البيانات في Firestore مع استخدام SetOptions للتأكد من عدم مسح بيانات قديمة
                    db.collection("users").document(uid)
                        .set(userUpdates, SetOptions.merge())
                        .addOnSuccessListener {
                            // تحديث اسم المستخدم في Firebase Auth (Profile)
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name.trim())
                                .build()

                            auth.currentUser?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener {
                                    isAdmin.value = shouldBeAdmin
                                    currentUser.value = auth.currentUser
                                    isLoading.value = false
                                    Log.d("AuthDebug", "تم إنشاء الحساب بنجاح: Admin=$shouldBeAdmin")
                                    onSuccess()
                                }
                        }
                        .addOnFailureListener { e ->
                            isLoading.value = false
                            errorMessage.value = "فشل حفظ البيانات: ${e.localizedMessage}"
                        }
                } else {
                    isLoading.value = false
                    errorMessage.value = "فشل الإنشاء: ${task.exception?.localizedMessage}"
                }
            }
    }

    fun logout() {
        auth.signOut()
        currentUser.value = null
        isAdmin.value = false
    }
}
