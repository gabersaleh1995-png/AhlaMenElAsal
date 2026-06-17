package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.ui.screens.LibraryItem
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getPdfs(): Flow<List<LibraryItem>>
}
