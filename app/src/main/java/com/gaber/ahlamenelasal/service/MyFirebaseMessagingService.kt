package com.gaber.ahlamenelasal.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gaber.ahlamenelasal.MainActivity
import com.gaber.ahlamenelasal.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // طباعة البيانات في الـ Logcat للتأكد من وصولها
        Log.d("Gaber_FCM", "وصلت رسالة جديدة من: ${remoteMessage.from}")
        Log.d("Gaber_FCM", "Data: ${remoteMessage.data}")
        Log.d("Gaber_FCM", "Notification: ${remoteMessage.notification?.body}")

        // محاولة استخراج العنوان والنص من أي من المصدرين (data أو notification)
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "أحلى من العسل 🐝"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "لديك رسالة جديدة"

        sendNotification(title, body)
    }

    override fun onNewToken(token: String) {
        Log.d("Gaber_FCM", "New Token: $token")
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "main_notifications_v1"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // استخدام الأيقونة الموجودة فعلياً
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setLights(Color.YELLOW, 3000, 3000)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات الدردشة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "استقبال رسائل الشات والاشعارات الهامة"
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
