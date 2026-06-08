package com.gaber.ahlamenelasal.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaber.ahlamenelasal.data.BibleRepository
import com.gaber.ahlamenelasal.data.BibleVerseJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // تحويل isDataReady لـ StateFlow ليكون التفاعل في الـ UI أفضل
    private val _isDataReady = MutableStateFlow(BibleRepository.isLoaded())
    val isDataReady = _isDataReady.asStateFlow()

    init {
        ensureBibleLoaded()
    }

    private fun ensureBibleLoaded() {
        if (!BibleRepository.isLoaded()) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    BibleRepository.loadBibleOnce(getApplication())
                    _isDataReady.value = BibleRepository.isLoaded()
                } catch (e: Exception) {
                    Log.e("Gaber_Bible", "❌ خطأ في التحميل: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun getVerses(bookName: String, chapter: Int): List<BibleVerseJson> {
        // التأكد من جاهزية البيانات أولاً
        if (!BibleRepository.isLoaded()) {
            Log.w("Gaber_Bible", "⚠️ محاولة الوصول للآيات قبل اكتمال التحميل")
            return emptyList()
        }

        val verses = BibleRepository.getVersesInstant(bookName.trim(), chapter)
        if (verses.isEmpty()) {
            Log.e("Gaber_Bible", "❌ لم يتم العثور على آيات لـ: $bookName أصحاح $chapter")
        } else {
            Log.d("Gaber_Bible", "✅ تم العثور على ${verses.size} آية")
        }
        return verses
    }

    fun getChapterCount(bookName: String): Int {
        return BibleRepository.getChapterCount(bookName)
    }
}
