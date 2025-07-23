package com.example.alarmkotlin

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {

    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmDebug", "AlarmActivity открыт!")

        setContentView(R.layout.activity_alarm) // <-- Убедись, что твой XML — activity_alarm.xml

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        ringtone = RingtoneManager.getRingtone(this, notificationUri)
        ringtone.play()
    }

    override fun onDestroy() {
        if (::ringtone.isInitialized && ringtone.isPlaying) {
            ringtone.stop()
        }
        super.onDestroy()
    }
}