package com.example.alarmkotlin.alarmList


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.alarmkotlin.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmDebug", "AlarmReceiver сработал")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlarmKotlin::AlarmWakelock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10 минут

        val ringtoneUri = intent?.getStringExtra("selected_ringtone")

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_ringtone", ringtoneUri)
        }

        context.startActivity(alarmIntent)
    }

}