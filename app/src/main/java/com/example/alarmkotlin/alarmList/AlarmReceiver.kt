package com.example.alarmkotlin.alarmList

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarmkotlin.AlarmActivity
import com.example.alarmkotlin.alarmList.AlarmService
import com.example.alarmkotlin.R

/**
 * AlarmReceiver — обновленный для работы на современных версиях Android
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_ID = 12345
        private const val CHANNEL_ID = "alarm_channel"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmDebug", "AlarmReceiver сработал")

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                // Восстанавливаем будильники после перезагрузки
                Log.d("AlarmDebug", "Устройство загружено, восстанавливаем будильники")
                // Здесь можно добавить логику восстановления будильников
                return
            }
        }

        // Основная логика срабатывания будильника
        handleAlarmTrigger(context, intent)
    }

    private fun handleAlarmTrigger(context: Context, intent: Intent?) {
        // 1. Получаем PowerManager для "пробуждения" устройства
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        // 2. Создаём более мощный WakeLock
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "AlarmKotlin::AlarmWakelock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10 минут

        // 3. Разблокируем экран если нужно
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                // keyguardManager.requestDismissKeyguard() требует Activity, поэтому пропускаем здесь
                // Разблокировка будет происходить в AlarmActivity
                Log.d("AlarmDebug", "Keyguard будет разблокирован в AlarmActivity")
            } catch (e: Exception) {
                Log.e("AlarmDebug", "Ошибка работы с keyguard: ${e.message}")
            }
        }

        // 4. Получаем рингтон
        val ringtoneUri = intent?.getStringExtra("selected_ringtone")

        // 5. Создаем уведомление с полноэкранным интентом
        createFullScreenNotification(context, ringtoneUri)

        // 6. Запускаем Foreground Service для надежности
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("selected_ringtone", ringtoneUri)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 7. Также пытаемся запустить активность напрямую
        launchAlarmActivity(context, ringtoneUri)
    }

    private fun createFullScreenNotification(context: Context, ringtoneUri: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for alarm app"
                setBypassDnd(true)
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Создаем Intent для полноэкранной активности
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("selected_ringtone", ringtoneUri)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление с полноэкранным интентом
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Будильник")
            .setContentText("Время будильника!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // КРИТИЧЕСКИ ВАЖНО!
            .setOngoing(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun launchAlarmActivity(context: Context, ringtoneUri: String?) {
        try {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
                putExtra("selected_ringtone", ringtoneUri)
            }

            context.startActivity(alarmIntent)
            Log.d("AlarmDebug", "AlarmActivity запущена")
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Ошибка запуска AlarmActivity: ${e.message}")
        }
    }
}