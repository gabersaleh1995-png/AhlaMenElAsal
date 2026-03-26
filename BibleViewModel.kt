package com.gaber.ahlamenelasal.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaber.ahlamenelasal.data.BibleBookJson
import com.gaber.ahlamenelasal.data.BibleRepository
import com.gaber.ahlamenelasal.data.BibleVerseJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val _bibleData = mutableStateOf<List<BibleBookJson>>(emptyList())
    val bibleData: State<List<BibleBookJson>> = _bibleData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadBible()
    }

    private fun loadBible() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = withContext(Dispatchers.IO) {
                BibleRepository.loadBible(getApplication())
            }
            _bibleData.value = data
            _isLoading.value = false
        }
    }

    fun getVerses(bookName: String, chapter: Int): List<BibleVerseJson> {
        val book = _bibleData.value.find { it.book == bookName }
        return book?.verses?.filter { it.chapter == chapter } ?: emptyList()
    }
}
