package com.example.alarmkotlin

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
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
import com.example.alarmkotlin.alarmList.AlarmReceiver
import com.example.alarmkotlin.alarmList.QuestionPackage.EasyQuestionList
import com.example.alarmkotlin.alarmList.QuestionPackage.HardQuestionList
import com.example.alarmkotlin.alarmList.QuestionPackage.MediumQuestionList
import com.example.alarmkotlin.alarmList.QuestionPackage.QuestionList
import com.example.alarmkotlin.databinding.ActivityAlarmBinding
import com.example.alarmkotlin.databinding.ActivityMainBinding
import java.util.Calendar

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

        // Проверяем причину срабатывания (сравниваем текущий день с выбранным)
        checkAlarmDayReason()

        getQuest()
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
            }else{
                getQuest()
                binding.answer.setText("")
            }

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

        // Получаем данные из intent для автоповтора
        val selectedDays = intent.getStringExtra("selected_days") ?: ""
        val ringtoneUri = intent.getStringExtra("selected_ringtone")
        val difficulty = intent.getIntExtra("selected_difficulty", 1)

        // Если выбраны определенные дни недели - перепланируем будильник на следующую неделю
        if (selectedDays.isNotBlank()) {
            scheduleNextWeekAlarm(selectedDays, ringtoneUri, difficulty)
        }

        stopAlarmSound()
        finish()
    }

    /**
     * Планирует будильник на следующую неделю для тех же дней (автоповтор)
     */
    private fun scheduleNextWeekAlarm(selectedDays: String, ringtoneUri: String?, difficulty: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val daysList = selectedDays.split(",").filter { it.isNotBlank() }

        // Получаем время из текущего intent
        val currentTime = intent.getStringExtra("alarm_time") ?: "07:00"
        val parts = currentTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        // Сопоставление кодов дней с Calendar константами
        val dayMapping = mapOf(
            "Mon" to Calendar.MONDAY,
            "Tue" to Calendar.TUESDAY,
            "Wed" to Calendar.WEDNESDAY,
            "Thu" to Calendar.THURSDAY,
            "Fri" to Calendar.FRIDAY,
            "Sat" to Calendar.SATURDAY,
            "Sun" to Calendar.SUNDAY
        )

        var alarmCount = 0
        val baseId = intent.getIntExtra("alarm_id", System.currentTimeMillis().toInt())

        // Устанавливаем будильник для каждого выбранного дня недели
        for (dayCode in daysList) {
            val targetDayOfWeek = dayMapping[dayCode] ?: continue

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Следующая неделя

            // Находим нужный день недели на следующей неделе
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysUntilTarget = targetDayOfWeek - currentDayOfWeek
            calendar.add(Calendar.DAY_OF_MONTH, daysUntilTarget)

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Уникальный requestCode для каждого дня недели
            val requestCode = baseId * 10 + alarmCount

            val newIntent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("selected_ringtone", ringtoneUri)
                putExtra("selected_difficulty", difficulty)
                putExtra("selected_days", selectedDays)
                putExtra("alarm_time", currentTime)
                putExtra("target_day", dayCode) // Конкретный день для проверки
                action = "ALARM_ACTION_$requestCode"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Устанавливаем точное срабатывание
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d("AlarmDebug", "Автоповтор: будильник установлен на следующий $dayCode: ${calendar.time}")
            alarmCount++
        }
    }


    /**
     * Проверка, совпадает ли день недели будильника с сегодняшним
     */
    private fun checkAlarmDayReason() {
        val selectedDays = intent.getStringExtra("selected_days") ?: ""
        if (selectedDays.isBlank()) {
            Log.d("AlarmDebug", "Будильник сработал без выбранных дней (одноразовый).")
            return
        }

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        val dayMapping = mapOf(
            "Mon" to Calendar.MONDAY,
            "Tue" to Calendar.TUESDAY,
            "Wed" to Calendar.WEDNESDAY,
            "Thu" to Calendar.THURSDAY,
            "Fri" to Calendar.FRIDAY,
            "Sat" to Calendar.SATURDAY,
            "Sun" to Calendar.SUNDAY
        )

        // Проверяем все выбранные дни
        val matchedDay = dayMapping.entries.firstOrNull { (code, calConst) ->
            selectedDays.contains(code) && today == calConst
        }

        if (matchedDay != null) {
            Log.d("AlarmDebug", "✅ Будильник сработал, потому что сегодня ${matchedDay.key}")
        } else {
            Log.w("AlarmDebug", "⚠ Будильник сработал, но сегодняшний день не совпадает с выбранными ($selectedDays)")
        }
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
    private fun getQuest() {
        // Правильное получение Int (значение по умолчанию = 1)
        val level = intent.getIntExtra("selected_difficulty", 1)

        val provider = when(level) {
            1 -> EasyQuestionList()
            2 -> MediumQuestionList()
            3 -> HardQuestionList()
            else -> EasyQuestionList() // fallback
        }

        binding.question.text = provider.getQuestion()
        trueAnsw = provider.getAnswer()
    }

    // Предотвращаем закрытие кнопкой "Назад" случайно
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // dismissAlarm()
    }
}