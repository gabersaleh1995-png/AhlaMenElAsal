package com.gaber.ahlamenelasal.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.gaber.ahlamenelasal.data.model.Meeting
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MeetingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val meetings = mutableStateListOf<Meeting>()

    init {
        fetchMeetings()
    }

    private fun fetchMeetings() {
        db.collection("meetings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    meetings.clear()
                    for (doc in snapshot.documents) {
                        val meeting = doc.toObject(Meeting::class.java)?.copy(id = doc.id)
                        if (meeting != null) {
                            meetings.add(meeting)
                        }
                    }
                }
            }
    }
}
