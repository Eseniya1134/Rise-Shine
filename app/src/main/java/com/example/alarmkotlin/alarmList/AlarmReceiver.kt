package com.example.alarmkotlin.alarmList

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.alarmkotlin.AlarmActivity

/**
 * AlarmReceiver — это BroadcastReceiver, который срабатывает, когда приходит сигнал от AlarmManager.
 * Именно отсюда запускается AlarmActivity при наступлении времени будильника.
 */
class AlarmReceiver : BroadcastReceiver() {

    /**
     * Метод вызывается системой, когда приходит запланированное событие (будильник).
     */
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmDebug", "AlarmReceiver сработал") // Для отладки — подтверждение срабатывания

        // Получаем PowerManager, чтобы "разбудить" устройство
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        // Создаём WakeLock, чтобы устройство не "уснуло" во время запуска AlarmActivity
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, // PARTIAL_WAKE_LOCK — не даёт CPU заснуть
            "AlarmKotlin::AlarmWakelock"    // тег для отладки
        )
        wakeLock.acquire(10 * 60 * 1000L) // Активный wake lock на 10 минут

        // Получаем выбранный пользователем рингтон из Intent
        val ringtoneUri = intent?.getStringExtra("selected_ringtone")

        // Создаём новый Intent для запуска AlarmActivity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_ringtone", ringtoneUri) // передаём рингтон в активность
        }

        // Запускаем AlarmActivity (так как мы вне активити, обязательно нужен флаг NEW_TASK)
        context.startActivity(alarmIntent)
    }
}
