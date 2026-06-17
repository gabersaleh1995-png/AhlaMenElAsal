package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, message: Map<String, Any>): Result<Unit>
    suspend fun updateChatMetadata(chatId: String, metadata: Map<String, Any>): Result<Unit>
    suspend fun markAsRead(chatId: String, currentUid: String): Result<Unit>
    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit>
    suspend fun deleteMessageForMe(chatId: String, messageId: String, currentUid: String): Result<Unit>
    fun getChatStatus(chatId: String): Flow<Map<String, Any>>
    suspend fun getAdminUserIds(): List<String>
    suspend fun getUserOneSignalId(userId: String): String?
}
