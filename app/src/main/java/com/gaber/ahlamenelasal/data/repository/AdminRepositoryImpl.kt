package com.gaber.ahlamenelasal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRepository {

    override fun getChatStatus(chatId: String): Flow<Map<String, Any>> = callbackFlow {
        val subscription = db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.data ?: emptyMap())
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveItem(collection: String, data: Map<String, Any>): Result<Unit> {
        return try {
            db.collection(collection).add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChatStatus(chatId: String, status: String, passcode: String): Result<Unit> {
        return try {
            db.collection("chats").document(chatId)
                .set(mapOf("status" to status, "passcode" to passcode), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
