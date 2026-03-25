package com.gaber.ahlamenelasal.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.gaber.ahlamenelasal.data.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.*

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val messages = mutableStateListOf<ChatMessage>()

    fun listenToMessages(chatId: String) {
        val currentUid = auth.currentUser?.uid ?: ""
        Log.d("ChatViewModel", "Start listening to: $chatId")
        
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Firestore Listen Error: ${error.message}")
                    if (error.message?.contains("index") == true) {
                        listenWithoutOrder(chatId, currentUid)
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("ChatViewModel", "Fetched ${snapshot.size()} messages")
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
                val msg = ChatMessage(
                    id = doc.id,
                    senderId = senderId,
                    senderName = doc.getString("senderName") ?: "مجهول",
                    message = doc.getString("message") ?: "",
                    voiceUrl = doc.getString("voiceUrl") ?: "",
                    type = doc.getString("type") ?: "TEXT",
                    timestamp = doc.getTimestamp("timestamp"),
                    isMe = senderId == currentUid,
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

    fun sendMessage(chatId: String, text: String, type: String = "TEXT", voiceUrl: String = "") {
        // السماح بإرسال الرسالة إذا كان هناك نص أو إذا كان هناك رابط صوت
        if (text.isBlank() && voiceUrl.isBlank()) return

        val user = auth.currentUser
        val senderName = user?.displayName ?: "مستخدم"
        val senderId = user?.uid ?: "anonymous"
        
        val newMessage = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "message" to text,
            "voiceUrl" to voiceUrl,
            "type" to type,
            "timestamp" to Timestamp.now(),
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
                Log.d("ChatViewModel", "Message sent successfully of type $type")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message", e)
            }
    }

    fun sendVoiceMessage(chatId: String, audioUri: Uri, onProgress: (Boolean) -> Unit) {
        onProgress(true)
        Log.d("ChatViewModel", "Starting voice upload to Cloudinary: $audioUri")
        
        MediaManager.get().upload(audioUri)
            .option("resource_type", "video")
            .option("folder", "voices/$chatId")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started")
                }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String ?: ""
                    Log.d("Cloudinary", "Upload success: $secureUrl")
                    sendMessage(chatId, "", "VOICE", secureUrl)
                    onProgress(false)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Upload error: ${error.description}")
                    onProgress(false)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    onProgress(false)
                }
            })
            .dispatch()
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
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Chat $chatId deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error deleting chat $chatId", e)
            }
    }
}
