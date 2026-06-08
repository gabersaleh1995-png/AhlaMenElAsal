package com.gaber.ahlamenelasal.util

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object NotificationHelper {
    private val client = OkHttpClient()
    
    // OneSignal API Key (Rest API Key)
    private const val ONESIGNAL_API_KEY = "os_v2_app_3m2pijrsvrajhbly25xya6acg4cerhlropwexifqgj6myh34im2supapacj3smr3phsem5mrj33ryihl5u57zg6pgp3dvzgtydcgg5a" 
    private const val ONESIGNAL_APP_ID = "db34f426-32ac-4093-8578-d76f80780237"
    private const val ONESIGNAL_URL = "https://onesignal.com/api/v1/notifications"

    fun sendNotification(to: String, title: String, body: String, scheduleTime: String? = null) {
        try {
            val json = JSONObject()
            json.put("app_id", ONESIGNAL_APP_ID)
            
            val contents = JSONObject()
            contents.put("en", body)
            contents.put("ar", body)
            json.put("contents", contents)

            val headings = JSONObject()
            headings.put("en", title)
            headings.put("ar", title)
            json.put("headings", headings)

            if (to == "all") {
                val segments = JSONArray()
                segments.put("Total Subscriptions")
                json.put("included_segments", segments)
            } else {
                val targetIds = JSONArray()
                targetIds.put(to)
                // في OneSignal v5، نستخدم include_subscription_ids فقط
                json.put("include_subscription_ids", targetIds)
            }

            // إضافة بيانات إضافية للتنبيه
            val data = JSONObject()
            data.put("type", "chat")
            json.put("data", data)
            
            // تحسين ظهور الإشعار
            json.put("android_visibility", 1)
            json.put("priority", 10)

            if (!scheduleTime.isNullOrBlank()) {
                json.put("send_after", scheduleTime)
            }

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(ONESIGNAL_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic $ONESIGNAL_API_KEY")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("NotificationHelper", "OneSignal Failure: ${e.message}")
                }
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    if (response.isSuccessful) {
                        Log.d("NotificationHelper", "OneSignal Success: $responseData")
                    } else {
                        Log.e("NotificationHelper", "OneSignal Error Response: $responseData")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("NotificationHelper", "OneSignal Error: ${e.message}")
        }
    }

    fun notifyAll(title: String, body: String) {
        sendNotification("all", title, body)
    }

    fun notifyAllScheduled(title: String, body: String, scheduleTime: String) {
        sendNotification("all", title, body, scheduleTime)
    }
}
