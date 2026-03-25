package com.gaber.ahlamenelasal.data.model

import com.google.firebase.Timestamp

data class Meeting(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
