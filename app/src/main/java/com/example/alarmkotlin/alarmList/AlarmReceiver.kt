package com.example.alarmkotlin.alarmList

import android.app.AlarmManager
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
import java.util.Calendar

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
        // Получаем выбранные дни недели из intent
        val selectedDaysStr = intent?.getStringExtra("selected_days") ?: ""

        // Если дни недели не указаны - запускаем будильник (как раньше)
        if (selectedDaysStr.isEmpty()) {
            Log.d("AlarmDebug", "Дни недели не указаны, запускаем будильник")
            proceedWithAlarm(context, intent)
            return
        }

        // Проверяем, должен ли будильник сработать сегодня
        if (shouldAlarmTriggerToday(selectedDaysStr)) {
            Log.d("AlarmDebug", "Будильник должен сработать сегодня")
            proceedWithAlarm(context, intent)

            // После срабатывания НЕ планируем на следующую неделю здесь!
            // Это будет делать AlarmActivity после отключения будильника
        } else {
            Log.d("AlarmDebug", "Будильник НЕ должен сработать сегодня")
            // Если это был неправильный день - планируем на следующий подходящий день
            scheduleNextOccurrence(context, intent)
        }
    }

    /**
     * Проверяет, должен ли будильник сработать сегодня
     */
    private fun shouldAlarmTriggerToday(selectedDaysStr: String): Boolean {
        val selectedDays = selectedDaysStr.split(",").filter { it.isNotBlank() }

        if (selectedDays.isEmpty()) {
            return true // Если дни не указаны - срабатываем каждый день
        }

        // Получаем текущий день недели
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Пн"
            Calendar.TUESDAY -> "Вт"
            Calendar.WEDNESDAY -> "Ср"
            Calendar.THURSDAY -> "Чт"
            Calendar.FRIDAY -> "Пт"
            Calendar.SATURDAY -> "Сб"
            Calendar.SUNDAY -> "Вс"
            else -> ""
        }

        Log.d("AlarmDebug", "Текущий день: $currentDayOfWeek, выбранные дни: $selectedDays")

        return currentDayOfWeek in selectedDays
    }

    /**
     * Планирует будильник на следующий подходящий день
     */
    private fun scheduleNextOccurrence(context: Context, intent: Intent?) {
        val selectedDaysStr = intent?.getStringExtra("selected_days") ?: ""
        val ringtoneUri = intent?.getStringExtra("selected_ringtone")
        val difficulty = intent?.getIntExtra("selected_difficulty", 1) ?: 1
        val timeStr = intent?.getStringExtra("alarm_time") ?: "07:00"

        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val selectedDays = selectedDaysStr.split(",").filter { it.isNotBlank() }
        if (selectedDays.isEmpty()) return

        // Сопоставление кодов дней с Calendar константами
        val dayMapping = mapOf(
            "Пн" to Calendar.MONDAY,
            "Вт" to Calendar.TUESDAY,
            "Ср" to Calendar.WEDNESDAY,
            "Чт" to Calendar.THURSDAY,
            "Пт" to Calendar.FRIDAY,
            "Сб" to Calendar.SATURDAY,
            "Вс" to Calendar.SUNDAY
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Находим следующий подходящий день
        var foundNextDay = false
        for (i in 1..7) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Пн"
                Calendar.TUESDAY -> "Вт"
                Calendar.WEDNESDAY -> "Ср"
                Calendar.THURSDAY -> "Чт"
                Calendar.FRIDAY -> "Пт"
                Calendar.SATURDAY -> "Сб"
                Calendar.SUNDAY -> "Вс"
                else -> ""
            }

            if (dayOfWeek in selectedDays) {
                foundNextDay = true
                break
            }
        }

        if (!foundNextDay) return

        // Получаем requestCode из action или создаем новый
        val requestCode = intent?.action?.substringAfter("ALARM_ACTION_")?.toIntOrNull()
            ?: System.currentTimeMillis().toInt()

        val newIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("selected_ringtone", ringtoneUri)
            putExtra("selected_difficulty", difficulty)
            putExtra("selected_days", selectedDaysStr)
            putExtra("alarm_time", timeStr)
            action = "ALARM_ACTION_$requestCode"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем будильник на следующий подходящий день
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d("AlarmDebug", "Будильник перенесен на следующий подходящий день: ${calendar.time}")
    }

    /**
     * Основная логика запуска будильника (вынесена из handleAlarmTrigger)
     */
    private fun proceedWithAlarm(context: Context, intent: Intent?) {
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

        // 4. Получаем рингтон \ уровень сложности
        val ringtoneUri = intent?.getStringExtra("selected_ringtone")
        val difficulty = intent?.getIntExtra("selected_difficulty", 1) ?: 1
        val daysOfWeek = intent?.getStringExtra("selected_days").toString()

        // 5. Создаем уведомление с полноэкранным интентом
        createFullScreenNotification(context, ringtoneUri, difficulty, daysOfWeek)

        // 6. Запускаем Foreground Service для надежности
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("selected_ringtone", ringtoneUri)
            putExtra("selected_difficulty", difficulty)
            putExtra("selected_days", daysOfWeek)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 7. Также пытаемся запустить активность напрямую
        launchAlarmActivity(context, ringtoneUri, difficulty, daysOfWeek)
    }

    private fun createFullScreenNotification(context: Context, ringtoneUri: String?, difficulty: Int, daysOfWeek: String) {
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
            putExtra("selected_difficulty", difficulty)
            putExtra("selected_days", daysOfWeek)
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

    private fun launchAlarmActivity(context: Context, ringtoneUri: String?, difficulty: Int, daysOfWeek: String) {
        try {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
                putExtra("selected_ringtone", ringtoneUri)
                putExtra("selected_difficulty", difficulty)
                putExtra("selected_days", daysOfWeek)
            }

            context.startActivity(alarmIntent)
            Log.d("AlarmDebug", "AlarmActivity запущена")
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Ошибка запуска AlarmActivity: ${e.message}")
        }
    }
}