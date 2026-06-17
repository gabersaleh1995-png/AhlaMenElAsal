package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.LibraryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LibraryRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LibraryRepository {

    override fun getPdfs(): Flow<List<LibraryItem>> = callbackFlow {
        val subscription = db.collection("bible_pdfs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LibraryItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(items)
            }
        
        awaitClose { subscription.remove() }
    }
}
