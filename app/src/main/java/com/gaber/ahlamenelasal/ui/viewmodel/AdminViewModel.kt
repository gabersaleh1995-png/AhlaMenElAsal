package com.gaber.ahlamenelasal.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.gaber.ahlamenelasal.util.NotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import androidx.documentfile.provider.DocumentFile
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    
    var isUploading = mutableStateOf(false)
    var uploadProgress = mutableStateOf(0f)

    // حالات غرفة الدردشة لمراقبتها في لوحة الأدمن
    var chatStatus = mutableStateOf("open")
    var chatPasscode = mutableStateOf("")

    init {
        listenToChatStatus("group_all")
    }

    private fun listenToChatStatus(chatId: String) {
        db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    chatStatus.value = snapshot.getString("status") ?: "open"
                    chatPasscode.value = snapshot.getString("passcode") ?: ""
                }
            }
    }

    fun saveDirectLink(context: Context, title: String, url: String, type: String, category: String = "", description: String = "", folder: String = "", subFolder: String = "") {
        saveInfoToFirestore(context, title, url, type, category, description, folder, subFolder)
    }

    fun uploadFileToCloudinary(
        context: Context, 
        uri: Uri, 
        fileType: String, 
        title: String, 
        category: String = "", 
        description: String = "",
        folder: String = "",
        subFolder: String = ""
    ) {
        val filePath = getRealPathFromUri(context, uri) ?: run {
            Toast.makeText(context, "فشل الوصول للملف", Toast.LENGTH_SHORT).show()
            return
        }
        isUploading.value = true
        uploadProgress.value = 0f
        
        val isPdf = filePath.endsWith(".pdf", ignoreCase = true) || fileType == "PDF"
        val isImage = fileType == "IMAGE" || fileType == "TOPIC_IMAGE"
        
        val resourceType = when {
            isImage -> "image"
            isPdf -> "raw" // تغيير PDF إلى raw لضمان الرفع كملف
            fileType == "AUDIO" || fileType == "VIDEO" || fileType == "TOPIC_VIDEO" -> "video"
            else -> "raw"
        }
        
        val fileExtension = when {
            fileType == "PDF" -> ".pdf"
            fileType == "AUDIO" -> ".mp3"
            fileType == "VIDEO" -> ".mp4"
            fileType == "IMAGE" || fileType == "TOPIC_IMAGE" -> ".jpg"
            fileType == "TOPIC_VIDEO" -> ".mp4"
            else -> ""
        }
        val publicId = "${fileType.lowercase()}_${UUID.randomUUID()}$fileExtension"

        MediaManager.get().upload(filePath)
            .unsigned("gaber_voice")
            .option("resource_type", resourceType)
            .option("public_id", publicId)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    uploadProgress.value = bytes.toFloat() / totalBytes.toFloat()
                }
                override fun onSuccess(requestId: String?, resultData: MutableMap<out Any?, Any?>?) {
                    var finalUrl = resultData?.get("secure_url").toString()
                    if (isPdf && !finalUrl.endsWith(".pdf", ignoreCase = true)) {
                        finalUrl = "$finalUrl.pdf"
                    }
                    saveInfoToFirestore(context, title, finalUrl, fileType, category, description, folder, subFolder)
                    isUploading.value = false
                    try { File(filePath).delete() } catch (e: Exception) {}
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    isUploading.value = false
                    Toast.makeText(context, "خطأ في الرفع: ${error?.description}", Toast.LENGTH_LONG).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun saveInfoToFirestore(
        context: Context,
        title: String, 
        url: String, 
        type: String, 
        category: String, 
        description: String = "",
        folder: String = "",
        subFolder: String = ""
    ) {
        val collection = when (type) {
            "VIDEO" -> "videos"
            "AUDIO" -> "audios"
            "PDF" -> "bible_pdfs"
            "IMAGE" -> "images"
            "TOPIC_IMAGE", "TOPIC_VIDEO" -> "topics"
            else -> "others"
        }

        val data = mutableMapOf<String, Any>(
            "title" to title,
            "timestamp" to FieldValue.serverTimestamp(), // استخدام وقت السيرفر
            "folder" to folder,
            "subFolder" to subFolder,
            "url" to url
        )

        if (type == "PDF") {
            data["category"] = if (category.isBlank()) "المكتبة العامة" else category
            data["fileType"] = "PDF"
        } else if (type.startsWith("TOPIC")) {
            data["description"] = description
            data["mediaType"] = if (type == "TOPIC_IMAGE") "IMAGE" else "VIDEO"
            data["mediaUrl"] = url
        }

        db.collection(collection).add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "تم الحفظ بنجاح: $title", Toast.LENGTH_SHORT).show()
                val (notifTitle, notifBody) = when(type) {
                    "TOPIC_IMAGE", "TOPIC_VIDEO" -> "موضوع جديد: $title 🍯" to (description.ifBlank { "شاهد الموضوع الجديد" }).take(100)
                    "PDF" -> "كتاب PDF جديد 📚" to "تم إضافة: $title في قسم المكتبة"
                    "IMAGE" -> "صورة جديدة في المعرض 🖼️" to "تم إضافة: $title"
                    "VIDEO" -> "فيديو جديد 🎬" to "تم إضافة: $title"
                    "AUDIO" -> "تسجيل صوتي جديد 🎙️" to "تم إضافة: $title"
                    else -> "محتوى جديد 🍯" to "تم إضافة: $title"
                }
                NotificationHelper.notifyAll(notifTitle, notifBody)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "فشل حفظ البيانات في Firestore: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val mimeType = contentResolver.getType(uri)
            
            var extension = when (mimeType) {
                "application/pdf" -> ".pdf"
                "image/jpeg" -> ".jpg"
                "image/png" -> ".png"
                "video/mp4" -> ".mp4"
                else -> ""
            }

            if (extension.isEmpty()) {
                val name = getFileName(context, uri)
                if (name.contains(".")) {
                    extension = "." + name.substringAfterLast(".")
                }
            }

            val tempFile = File(context.cacheDir, "temp_${UUID.randomUUID()}$extension")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
            tempFile.absolutePath
        } catch (e: Exception) { null }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        return name
    }

    fun lockChat(chatId: String, passcode: String, isLocked: Boolean) {
        val status = if (isLocked) "locked" else "open"
        db.collection("chats").document(chatId)
            .set(mapOf("status" to status, "passcode" to passcode), SetOptions.merge())
            .addOnSuccessListener {
                if (isLocked) {
                    NotificationHelper.notifyAll("تنبيه أمان ⚠️", "تم قفل الدردشة الجماعية 🔒. برجاء طلب الكود من الإدارة.")
                } else {
                    NotificationHelper.notifyAll("تنبيه 🔓", "تم فتح الدردشة الجماعية الآن للجميع ✨")
                }
            }
    }

    fun uploadFolderContent(context: Context, treeUri: Uri, category: String) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return
        val folderName = rootDoc.name ?: "مجلد جديد"
        
        isUploading.value = true
        val files = rootDoc.listFiles()

        if (files.isEmpty()) {
            isUploading.value = false
            return
        }

        files.forEach { doc ->
            if (doc.isDirectory) {
                val subFolderName = doc.name ?: "مجلد فرعي"
                doc.listFiles().forEach { subDoc ->
                    if (subDoc.isFile && subDoc.name?.lowercase()?.endsWith(".pdf") == true) {
                        uploadFileFromDocument(context, subDoc, category, folderName, subFolderName)
                    }
                }
            } else if (doc.isFile && doc.name?.lowercase()?.endsWith(".pdf") == true) {
                uploadFileFromDocument(context, doc, category, folderName, "")
            }
        }
    }

    private fun uploadFileFromDocument(context: Context, doc: DocumentFile, category: String, folder: String, subFolder: String) {
        val uri = doc.uri
        val name = doc.name ?: "ملف"
        if (name.lowercase().endsWith(".pdf")) {
            uploadFileToCloudinary(context, uri, "PDF", name.substringBeforeLast("."), category, folder = folder, subFolder = subFolder)
        }
    }
}
