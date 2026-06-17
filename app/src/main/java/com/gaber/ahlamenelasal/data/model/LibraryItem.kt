package com.gaber.ahlamenelasal.data.model

import com.google.firebase.firestore.PropertyName

data class LibraryItem(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val folder: String = "",
    val subFolder: String = "",
    val timestamp: Long = 0
)
