package com.gaber.ahlamenelasal.data.repository

import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun getChatStatus(chatId: String): Flow<Map<String, Any>>
    suspend fun saveItem(collection: String, data: Map<String, Any>): Result<Unit>
    suspend fun updateChatStatus(chatId: String, status: String, passcode: String): Result<Unit>
}
