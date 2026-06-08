package com.gaber.ahlamenelasal.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    // خريطة سريعة جداً للوصول للآيات: اسم السفر -> (رقم الأصحاح -> قائمة الآيات)
    private var fastBibleMap: Map<String, Map<Int, List<BibleVerseJson>>>? = null

    suspend fun loadBibleOnce(context: Context) = withContext(Dispatchers.IO) {
        if (fastBibleMap == null) {
            try {
                Log.d("Gaber_Bible", "⚡ بدأنا عملية الفهرسة الذكية...")
                val startTime = System.currentTimeMillis()
                
                Log.d("Gaber_Bible", "بدأنا نقرأ الملف...")
                val jsonString = context.assets.open("bible_arabic.json").bufferedReader().use { it.readText() }
                Log.d("Gaber_Bible", "تمت القراءة، طول النص: ${jsonString.length}")

                val listType = object : TypeToken<List<BibleBookJson>>() {}.type
                val books: List<BibleBookJson> = Gson().fromJson(jsonString, listType)
                
                // تحويل القائمة لخريطة (Indexing) لسرعة الوصول اللحظي O(1)
                fastBibleMap = books.associate { bookItem ->
                    bookItem.book.trim() to bookItem.verses.groupBy { it.chapter }
                }
                
                val endTime = System.currentTimeMillis()
                Log.d("Gaber_Bible", "✅ تم الفهرسة في ${endTime - startTime} مللي ثانية")
            } catch (e: Exception) {
                Log.e("Gaber_Bible", "❌ خطأ في تحميل الكتاب المقدس: ${e.message}")
            }
        }
    }

    // الوصول اللحظي O(1)
    fun getVersesInstant(bookName: String, chapter: Int): List<BibleVerseJson> {
        return fastBibleMap?.get(bookName.trim())?.get(chapter) ?: emptyList()
    }

    fun getChapterCount(bookName: String): Int {
        return fastBibleMap?.get(bookName.trim())?.size ?: 0
    }
    
    fun isLoaded(): Boolean = fastBibleMap != null
}
