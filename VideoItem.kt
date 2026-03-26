package com.gaber.ahlamenelasal.data.model

import com.google.firebase.Timestamp

data class VideoItem(
    val id: String = "",
    val title: String = "",
    val url: String = "", // رابط يوتيوب أو غيره
    val category: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
