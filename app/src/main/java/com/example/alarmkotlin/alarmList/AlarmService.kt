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
* AlarmService - Foreground Service для надежного запуска будильника
* Защищает от убийства системой и гарантирует запуск AlarmActivity
*/
class AlarmService : Service() {

    companion object {
        // Уникальный ID для уведомления сервиса (чтобы не конфликтовать с другими)
        private const val NOTIFICATION_ID = 12346
        // ID канала уведомлений для Android 8+
        private const val CHANNEL_ID = "alarm_service_channel"
    }

    /**
     * onCreate() - инициализация сервиса (вызывается один раз)
     * Создает канал уведомлений для Android 8+
     */
    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmDebug", "AlarmService создан")
        createNotificationChannel() // Создаем канал уведомлений для Android 8+
    }

    /**
     * onStartCommand() - основная логика сервиса (каждый запуск)
     * Переводит в Foreground режим и запускает AlarmActivity
     * Возвращает START_NOT_STICKY = не перезапускать после убийства
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmDebug", "AlarmService запущен")

        val ringtoneUri = intent?.getStringExtra("selected_ringtone")// Извлекаем URI рингтона

        // КРИТИЧЕСКИ ВАЖНО: переводим сервис в Foreground режим
        // Это защищает его от убийства системой и показывает постоянное уведомление
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

    override fun onBind(intent: Intent?): IBinder? = null//возвращаем null (не поддерживаем связанный сервис)

    /**
     * createNotificationChannel() - создает канал уведомлений (Android 8+)
     * Обязательно для всех уведомлений с API 26+
     */
    private fun createNotificationChannel() {
        // Каналы только для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, // ID канала
                "Alarm Service", // Название (видит пользователь)
                NotificationManager.IMPORTANCE_LOW // Низкая важность (без звука)
            ).apply {
                description = "Background service for alarm app"
                setShowBadge(false) // Без значка на иконке приложения
            }

            // Регистрируем в системе
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * createServiceNotification() - создает уведомление для Foreground Service
     * Обязательно для работы в приоритетном режиме
     */
    private fun createServiceNotification(): Notification {
        // Intent для возврата в приложение при нажатии на уведомление
        val intent = Intent(this, MainActivity::class.java)

        // PendingIntent - "отложенный" Intent для выполнения системой
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Service")
            .setContentText("Будильник активен")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent) // Действие при нажатии
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Постоянное (нельзя смахнуть)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmDebug", "AlarmService уничтожен")
    }
}