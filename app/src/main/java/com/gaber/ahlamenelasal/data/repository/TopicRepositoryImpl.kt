package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TopicRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TopicRepository {

    override fun getTopics(): Flow<List<Topic>> = callbackFlow {
        val subscription = db.collection("topics")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Topic::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(items)
            }
        
        awaitClose { subscription.remove() }
    }
}
