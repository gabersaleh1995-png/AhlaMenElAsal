package com.gaber.ahlamenelasal.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class AgbeyaPrayerJson(
    val title: String,
    val content: String
)

object AgbeyaRepository {
    private var agbeyaData: List<AgbeyaPrayerJson>? = null

    fun loadAgbeya(context: Context): List<AgbeyaPrayerJson> {
        if (agbeyaData != null) return agbeyaData!!
        
        return try {
            val inputStream = context.assets.open("agbeya.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<AgbeyaPrayerJson>>() {}.type
            agbeyaData = Gson().fromJson(reader, type)
            agbeyaData ?: emptyList()
        } catch (e: Exception) {
            // Fallback to hardcoded if file not found or error
            val fallback = listOf(
                AgbeyaPrayerJson("باكر", "صلاة باكر..."),
                AgbeyaPrayerJson("الثالثة", "صلاة الساعة الثالثة...")
            )
            fallback
        }
    }

    fun getPrayerContent(context: Context, prayerName: String): String {
        val prayers = loadAgbeya(context)
        return prayers.find { it.title == prayerName }?.content ?: "النص غير متوفر حالياً"
    }
}
