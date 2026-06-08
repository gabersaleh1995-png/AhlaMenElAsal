package com.gaber.ahlamenelasal.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.gaber.ahlamenelasal.data.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TopicsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var topics = mutableStateOf<List<Topic>>(emptyList())
    var isLoading = mutableStateOf(false)

    init {
        fetchTopics()
    }

    fun fetchTopics() {
        isLoading.value = true
        db.collection("topics")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                isLoading.value = false
                if (error != null) return@addSnapshotListener
                
                topics.value = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Topic::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }
}
