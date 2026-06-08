package com.gaber.ahlamenelasal.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.gaber.ahlamenelasal.data.model.ChatMessage
import com.gaber.ahlamenelasal.util.NotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.io.File

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val messages = mutableStateListOf<ChatMessage>()

    fun listenToMessages(chatId: String) {
        val currentUid = auth.currentUser?.uid ?: ""
        
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.message?.contains("index") == true) {
                        listenWithoutOrder(chatId, currentUid)
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    updateMessagesList(snapshot.documents, currentUid)
                }
            }
    }

    private fun listenWithoutOrder(chatId: String, currentUid: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val sortedDocs = snapshot.documents.sortedBy { it.getTimestamp("timestamp") }
                    updateMessagesList(sortedDocs, currentUid)
                }
            }
    }

    private fun updateMessagesList(documents: List<com.google.firebase.firestore.DocumentSnapshot>, currentUid: String) {
        val newMessages = documents.mapNotNull { doc ->
            try {
                val senderId = doc.getString("senderId") ?: ""
                val type = doc.getString("type") ?: "TEXT"
                val voiceUrl = doc.getString("voiceUrl") ?: ""
                
                if (type == "VOICE") {
                    if (voiceUrl.isBlank() || voiceUrl == "null" || !voiceUrl.startsWith("http")) {
                        Log.w("Gaber_Dev", "تخطي رسالة صوتية تالفة: ${doc.id}")
                        return@mapNotNull null
                    }
                }

                val msg = ChatMessage(
                    id = doc.id,
                    senderId = senderId,
                    senderName = doc.getString("senderName") ?: "مجهول",
                    message = doc.getString("message") ?: "",
                    voiceUrl = voiceUrl,
                    voiceDuration = (doc.get("voiceDuration") as? Number)?.toInt() ?: 0,
                    type = type,
                    timestamp = doc.getTimestamp("timestamp"),
                    isMe = senderId == currentUid,
                    isRead = doc.getBoolean("isRead") ?: false,
                    deletedFor = doc.get("deletedFor") as? List<String> ?: emptyList()
                )
                if (!msg.deletedFor.contains(currentUid)) msg else null
            } catch (e: Exception) {
                null
            }
        }
        messages.clear()
        messages.addAll(newMessages)
    }

    fun markMessagesAsRead(chatId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereNotEqualTo("senderId", currentUid)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.update("isRead", true)
                }
            }
    }

    fun sendMessage(chatId: String, text: String, type: String = "TEXT", voiceUrl: String = "", voiceDuration: Int = 0) {
        if (type == "VOICE" && (voiceUrl.isBlank() || voiceUrl == "null" || !voiceUrl.startsWith("http"))) {
            return
        }
        
        if (text.isBlank() && type != "VOICE") return

        val user = auth.currentUser
        val senderName = user?.displayName ?: "مستخدم"
        val senderId = user?.uid ?: "anonymous"
        
        val newMessage = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "message" to text,
            "voiceUrl" to voiceUrl,
            "voiceDuration" to voiceDuration,
            "type" to type,
            "timestamp" to Timestamp.now(),
            "isRead" to false,
            "deletedFor" to emptyList<String>()
        )

        val chatDocRef = db.collection("chats").document(chatId)
        val lastMsgText = if (type == "VOICE") "🎤 رسالة صوتية" else text
        
        val chatMetadata = mutableMapOf<String, Any>(
            "lastUpdate" to Timestamp.now(),
            "lastMessage" to lastMsgText
        )
        
        if (chatId.startsWith("admin_")) {
            chatMetadata["senderName"] = senderName
        }

        chatDocRef.set(chatMetadata, SetOptions.merge())
        chatDocRef.collection("messages").add(newMessage)
            .addOnSuccessListener { 
                // إرسال إشعار بتنسيق أجمل
                val notificationTitle = if (chatId == "group_all") "رسالة في المجموعة 👥" else "رسالة جديدة من $senderName 📩"
                sendChatNotification(chatId, notificationTitle, lastMsgText)
            }
    }

    private fun sendChatNotification(chatId: String, title: String, messageText: String) {
        if (chatId == "group_all") {
            NotificationHelper.sendNotification("all", title, messageText)
        } else if (chatId.startsWith("admin_")) {
            val userIdFromChatId = chatId.replace("admin_", "")
            val currentUid = auth.currentUser?.uid ?: return
            
            if (currentUid == userIdFromChatId) {
                // المستخدم يرسل للأدمن -> نبحث عن الأدمن
                db.collection("users").whereEqualTo("isAdmin", true).get()
                    .addOnSuccessListener { querySnapshot ->
                        for (doc in querySnapshot.documents) {
                            val targetId = doc.getString("oneSignalId") ?: doc.getString("fcmToken")
                            if (!targetId.isNullOrBlank()) {
                                NotificationHelper.sendNotification(targetId, title, messageText)
                            }
                        }
                    }
            } else {
                // الأدمن يرسل للمستخدم
                db.collection("users").document(userIdFromChatId).get()
                    .addOnSuccessListener { doc ->
                        val targetId = doc.getString("oneSignalId") ?: doc.getString("fcmToken")
                        if (!targetId.isNullOrBlank()) {
                            NotificationHelper.sendNotification(targetId, title, messageText)
                        }
                    }
            }
        }
    }

    fun sendVoiceMessage(chatId: String, filePath: String, onResult: (isUploading: Boolean, error: String?) -> Unit) {
        onResult(true, null)

        MediaManager.get().upload(filePath)
            .unsigned("gaber_voice") 
            .option("resource_type", "video") 
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<out Any?, Any?>?) {
                    val realUrl = resultData?.get("secure_url")?.toString()
                    val duration = (resultData?.get("duration") as? Number)?.toInt() ?: 0

                    if (!realUrl.isNullOrBlank() && realUrl != "null") {
                        sendMessage(chatId, "", "VOICE", realUrl, duration)
                    }
                    onResult(false, null)
                    try { File(filePath).delete() } catch (e: Exception) {}
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onResult(false, "فشل الرفع: ${error?.description}")
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    fun deleteMessageForAll(chatId: String, messageId: String) {
        db.collection("chats").document(chatId).collection("messages").document(messageId).delete()
    }

    fun deleteMessageForMe(chatId: String, messageId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("chats").document(chatId).collection("messages").document(messageId)
            .update("deletedFor", FieldValue.arrayUnion(currentUid))
    }

    fun deleteChat(chatId: String) {
        db.collection("chats").document(chatId).delete()
    }

    fun lockChat(chatId: String, passcode: String, isLocked: Boolean) {
        val status = if (isLocked) "locked" else "open"
        db.collection("chats").document(chatId)
            .set(mapOf("status" to status, "passcode" to passcode), SetOptions.merge())
    }

    fun listenToChatStatus(chatId: String, onStatusChange: (String, String) -> Unit) {
        db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                val status = snapshot?.getString("status") ?: "open"
                val passcode = snapshot?.getString("passcode") ?: ""
                onStatusChange(status, passcode)
            }
    }
}
