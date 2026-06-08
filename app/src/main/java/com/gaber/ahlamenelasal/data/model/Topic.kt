package com.gaber.ahlamenelasal.data.model

import com.google.firebase.Timestamp

data class Topic(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "", // "IMAGE" or "VIDEO"
    val timestamp: Timestamp = Timestamp.now()
)
