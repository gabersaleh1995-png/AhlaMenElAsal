package com.gaber.ahlamenelasal.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    var id: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var message: String = "", 
    var voiceUrl: String = "", 
    var type: String = "TEXT", 
    var timestamp: Timestamp? = null,
    var isMe: Boolean = false,
    var deletedFor: List<String> = emptyList()
)
