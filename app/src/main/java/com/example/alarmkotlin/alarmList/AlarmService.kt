package com.example.alarmkotlin.alarmList

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarmkotlin.AlarmActivity
import com.example.alarmkotlin.MainActivity
import com.example.alarmkotlin.R

/**
 * Foreground Service для надежной работы будильника в фоне
 */
class AlarmService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 12346
        private const val CHANNEL_ID = "alarm_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmDebug", "AlarmService создан")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmDebug", "AlarmService запущен")

        val ringtoneUri = intent?.getStringExtra("selected_ringtone")

        // Запускаем как Foreground Service
        startForeground(NOTIFICATION_ID, createServiceNotification())

        // Запускаем AlarmActivity
        val alarmIntent = Intent(this, AlarmActivity::class.java)
        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        alarmIntent.putExtra("selected_ringtone", ringtoneUri)

        try {
            startActivity(alarmIntent)
            Log.d("AlarmDebug", "AlarmActivity запущена из сервиса")
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Ошибка запуска AlarmActivity из сервиса: ${e.message}")
        }


        // Останавливаем сервис через 30 секунд
        android.os.Handler(mainLooper).postDelayed({
            stopSelf()
        }, 30000)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for alarm app"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createServiceNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Service")
            .setContentText("Будильник активен")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmDebug", "AlarmService уничтожен")
    }
}