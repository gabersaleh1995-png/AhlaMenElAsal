package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.VideoItem
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getVideos(): Flow<List<VideoItem>>
}
