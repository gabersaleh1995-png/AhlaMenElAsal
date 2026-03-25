package com.gaber.ahlamenelasal.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

data class BibleVerseJson(
    val chapter: Int,
    val verse: Int,
    val text: String
)

data class BibleBookJson(
    val book: String,
    val verses: List<BibleVerseJson>
)

object BibleRepository {
    private var bibleData: List<BibleBookJson>? = null

    suspend fun loadBible(context: Context): List<BibleBookJson> = withContext(Dispatchers.IO) {
        if (bibleData != null) return@withContext bibleData!!
        
        return@withContext try {
            Log.d("BibleRepository", "Loading bible_arabic.json from assets...")
            val inputStream = context.assets.open("bible_arabic.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<BibleBookJson>>() {}.type
            bibleData = Gson().fromJson(reader, type)
            Log.d("BibleRepository", "Loaded ${bibleData?.size} books successfully")
            bibleData ?: emptyList()
        } catch (e: Exception) {
            Log.e("BibleRepository", "Error loading bible: ${e.message}")
            emptyList()
        }
    }

    // نسخة غير معلقة للاستخدام السريع إذا تم التحميل مسبقاً
    fun getCachedBible(): List<BibleBookJson>? = bibleData

    suspend fun getVerses(context: Context, bookName: String, chapter: Int): List<BibleVerseJson> {
        val books = loadBible(context)
        val cleanBookName = bookName.trim()
        val book = books.find { it.book.trim() == cleanBookName }
        return book?.verses?.filter { it.chapter == chapter } ?: emptyList()
    }
}
