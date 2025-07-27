package com.example.alarmkotlin

import android.app.KeyguardManager
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * AlarmActivity — обновленная версия для работы при выключенном экране
 */
class AlarmActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmDebug", "AlarmActivity открыт!")

        // Настройки для показа поверх экрана блокировки
        setupWindowFlags()

        setContentView(R.layout.activity_alarm)

        // Запускаем звук
        startAlarmSound()

        // Настройка UI для закрытия будильника
        setupDismissButton()
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            // Разблокируем экран программно
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // Флаги окна для старых версий Android
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Для полноэкранного режима
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }

    private fun startAlarmSound() {
        val uriStr = intent.getStringExtra("selected_ringtone")
        val alarmUri = if (!uriStr.isNullOrEmpty()) {
            Uri.parse(uriStr)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmActivity, alarmUri)
                setAudioStreamType(AudioManager.STREAM_ALARM)
                isLooping = true

                // Устанавливаем максимальную громкость
                val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

                prepare()
                start()

                Log.d("AlarmDebug", "Звук будильника запущен")
            }
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Ошибка воспроизведения звука: ${e.message}")

            // Попытка воспроизвести системный звук как резерв
            try {
                val ringtone = RingtoneManager.getRingtone(this, alarmUri)
                ringtone?.play()
            } catch (ex: Exception) {
                Log.e("AlarmDebug", "Ошибка воспроизведения резервного звука: ${ex.message}")
            }
        }
    }

    private fun setupDismissButton() {
        // Добавляем возможность закрыть будильник нажатием на экран
        findViewById<View>(android.R.id.content)?.setOnClickListener {
            dismissAlarm()
        }

        // Автоматическое закрытие через 10 минут
        android.os.Handler(mainLooper).postDelayed({
            if (!isFinishing) {
                dismissAlarm()
            }
        }, 10 * 60 * 1000) // 10 минут
    }

    private fun dismissAlarm() {
        Log.d("AlarmDebug", "Закрываем будильник")
        stopAlarmSound()
        finish()
    }

    private fun stopAlarmSound() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                Log.d("AlarmDebug", "Звук остановлен")
            } catch (e: Exception) {
                Log.e("AlarmDebug", "Ошибка остановки звука: ${e.message}")
            }
        }
        mediaPlayer = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Если активность уже запущена, просто обновляем звук
        if (mediaPlayer?.isPlaying != true) {
            startAlarmSound()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }

    // Предотвращаем закрытие кнопкой "Назад" случайно
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        dismissAlarm()
    }
}