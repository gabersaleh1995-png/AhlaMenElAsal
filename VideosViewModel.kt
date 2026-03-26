package com.gaber.ahlamenelasal.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.gaber.ahlamenelasal.data.model.VideoItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class VideosViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val videos = mutableStateListOf<VideoItem>()

    init {
        fetchVideos()
    }

    private fun fetchVideos() {
        db.collection("videos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    videos.clear()
                    for (doc in snapshot.documents) {
                        val video = doc.toObject(VideoItem::class.java)?.copy(id = doc.id)
                        if (video != null) {
                            videos.add(video)
                        }
                    }
                }
            }
    }
}
