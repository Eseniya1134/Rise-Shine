package com.example.alarmkotlin

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * AlarmActivity — активность, отображаемая при срабатывании будильника.
 * Отвечает за включение экрана и воспроизведение звука.
 */
class AlarmActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null // Плеер для воспроизведения мелодии будильника

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmDebug", "AlarmActivity открыт!") // Для отладки

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )


        // Настройки окна, чтобы будильник показался даже при заблокированном экране:
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or // Показать при заблокированном экране
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or   // Включить экран
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON      // Не выключать экран
        )

        setContentView(R.layout.activity_alarm)
        // Получаем URI мелодии из интента (если пользователь выбирал рингтон)
        val uriStr = intent.getStringExtra("selected_ringtone")

        // Если рингтон не выбран — используем системный звук будильника по умолчанию
        val alarmUri = if (!uriStr.isNullOrEmpty()) Uri.parse(uriStr)
        else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        try {
            // Создаём и настраиваем MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmActivity, alarmUri)                 // источник звука
                setAudioStreamType(AudioManager.STREAM_ALARM)              // поток типа "будильник"
                isLooping = true                                           // зацикливаем звук
                prepare()                                                  // подготавливаем
                start()                                                    // запускаем
            }
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Ошибка воспроизведения звука: ${e.message}")
        }
    }

    /**
     * При уничтожении активности останавливаем и освобождаем плеер.
     * Это обязательно, чтобы звук не продолжал играть.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()   // Останавливаем звук
            }
            it.release()    // Освобождаем ресурсы
        }
    }
}
