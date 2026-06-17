package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.VideoItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class VideoRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : VideoRepository {

    override fun getVideos(): Flow<List<VideoItem>> = callbackFlow {
        val subscription = db.collection("videos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VideoItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(items)
            }
        
        awaitClose { subscription.remove() }
    }
}
