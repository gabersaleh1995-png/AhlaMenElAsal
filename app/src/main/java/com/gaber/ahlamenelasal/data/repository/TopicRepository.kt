package com.gaber.ahlamenelasal.data.repository

import com.gaber.ahlamenelasal.data.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    fun getTopics(): Flow<List<Topic>>
}
