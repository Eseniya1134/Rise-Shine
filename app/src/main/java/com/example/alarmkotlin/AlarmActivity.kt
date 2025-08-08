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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmkotlin.alarmList.QwestionList
import com.example.alarmkotlin.databinding.ActivityAlarmBinding
import com.example.alarmkotlin.databinding.ActivityMainBinding

/**
 * AlarmActivity - экран будильника, отображается поверх блокировки
 * Включает экран, воспроизводит звук и позволяет отключить будильник
 */
class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var trueAnsw: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmDebug", "AlarmActivity открыт!")


        // Настройки для показа поверх экрана блокировки
        setupWindowFlags()

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getQwest()
        // Запускаем звук
        startAlarmSound()

        // Настройка UI для закрытия будильника
        setupDismissButton()
    }

    /**
     * setupWindowFlags() - настройка окна для работы при заблокированном экране
     * Включает экран и показывает активность поверх блокировки
     */
    private fun setupWindowFlags() {
        // Для Android 8.1+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true) // Показать при заблокированном экране
            setTurnScreenOn(true)   // Включить экран

            // Программно разблокируем экран
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // Флаги для старых версий Android
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or     // Показать на блокировке
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or       // Включить экран
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or       // Не выключать экран
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or     // Убрать блокировку
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Полноэкранный режим
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

    /**
     * startAlarmSound() - запуск звука будильника
     * Использует выбранный рингтон или системный звук по умолчанию
     */
    private fun startAlarmSound() {
        // Получаем URI рингтона из Intent
        val uriStr = intent.getStringExtra("selected_ringtone")
        val alarmUri = if (!uriStr.isNullOrEmpty()) {
            Uri.parse(uriStr)
        } else {
            // Если рингтон не выбран, используем системные звуки по порядку
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmActivity, alarmUri)
                setAudioStreamType(AudioManager.STREAM_ALARM) // Поток будильника
                isLooping = true // Зацикливаем воспроизведение

                // Устанавливаем максимальную громкость для будильника
                val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

                prepare()
                start()

                Log.d("AlarmDebug", "Звук будильника запущен")
            }
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Ошибка воспроизведения звука: ${e.message}")

            // Резервный вариант - системный Ringtone
            try {
                val ringtone = RingtoneManager.getRingtone(this, alarmUri)
                ringtone?.play()
            } catch (ex: Exception) {
                Log.e("AlarmDebug", "Ошибка воспроизведения резервного звука: ${ex.message}")
            }
        }
    }

    /**
     * setupDismissButton() - настройка способов отключения будильника
     */
    private fun setupDismissButton() {

        //отключение по кнопке стоп
        binding.stopBtn.setOnClickListener { btn ->
            val userAnswer = binding.answer.text.toString().trim() // преобразуем в String и убираем пробелы
            if (userAnswer.equals(trueAnsw, ignoreCase = true)) { // добавляем ignoreCase для регистронезависимого сравнения
                dismissAlarm()
            }
            binding.answer.setText("")
        }

        // Автоматическое отключение через 10 минут
        android.os.Handler(mainLooper).postDelayed({
            if (!isFinishing) {
                dismissAlarm()
            }
        }, 10 * 60 * 1000)
    }

    private fun dismissAlarm() { //отключение будильника
        Log.d("AlarmDebug", "Закрываем будильник")
        stopAlarmSound()
        finish()
    }

    private fun stopAlarmSound() {  //остановка воспроизведения звука
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

    override fun onNewIntent(intent: Intent) {  //обновление звука при повторном запуске активности
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

    /**
     * Получение задачи и ответа для будильников
     */
    private fun getQwest(){
        var list = QwestionList()
        binding.question.text = list.getQuestion()
        trueAnsw = list.getAnswer()

    }

    // Предотвращаем закрытие кнопкой "Назад" случайно
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
       // dismissAlarm()
    }
}