package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.ChatMessage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback if index not ready
                    db.collection("chats").document(chatId).collection("messages")
                        .addSnapshotListener { snap, _ ->
                            val items = snap?.documents?.sortedBy { it.getTimestamp("timestamp") }?.mapNotNull { doc ->
                                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                            } ?: emptyList()
                            trySend(items)
                        }
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(chatId: String, message: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("chats").document(chatId).collection("messages").add(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChatMetadata(chatId: String, metadata: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("chats").document(chatId).set(metadata, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(chatId: String, currentUid: String): Result<Unit> {
        return try {
            val unread = db.collection("chats").document(chatId).collection("messages")
                .whereNotEqualTo("senderId", currentUid)
                .whereEqualTo("isRead", false)
                .get().await()
            for (doc in unread.documents) {
                doc.reference.update("isRead", true).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        return try {
            db.collection("chats").document(chatId).collection("messages").document(messageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessageForMe(chatId: String, messageId: String, currentUid: String): Result<Unit> {
        return try {
            db.collection("chats").document(chatId).collection("messages").document(messageId)
                .update("deletedFor", FieldValue.arrayUnion(currentUid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getChatStatus(chatId: String): Flow<Map<String, Any>> = callbackFlow {
        val sub = db.collection("chats").document(chatId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) trySend(snapshot.data ?: emptyMap())
        }
        awaitClose { sub.remove() }
    }

    override suspend fun getAdminUserIds(): List<String> {
        return try {
            val admins = db.collection("users").whereEqualTo("isAdmin", true).get().await()
            admins.documents.mapNotNull { it.getString("oneSignalId") ?: it.getString("fcmToken") }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getUserOneSignalId(userId: String): String? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.getString("oneSignalId") ?: doc.getString("fcmToken")
        } catch (e: Exception) { null }
    }
}
