package com.example.alarmkotlin.alarmList

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.alarmkotlin.AlarmActivity
import com.example.alarmkotlin.MainActivity
import com.example.alarmkotlin.alarmList.data.AlarmDatabase
import com.example.alarmkotlin.databinding.FragmentAddItemAlarmBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.Calendar

class AddItemAlarmFragment : Fragment() {

    private var _binding: FragmentAddItemAlarmBinding? = null
    private val binding get() = _binding!!

    // Хранение выбранных дней недели (Mon, Tue и т.д.)
    private val selectedDays = mutableSetOf<String>()

    private lateinit var db: AlarmDatabase // База данных
    // Хранение URI выбранной мелодии
    private var selectedRingtoneUri: String? = null

    // Таймпикер для выбора времени
    private lateinit var picker: MaterialTimePicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Привязка layout через ViewBinding
        _binding = FragmentAddItemAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alarmId = arguments?.getInt("alarm_id")

        //Загрузка уже имеющейся информации при редактировании
        lifecycleScope.launch {
            db = AlarmDatabase.getDatabase(requireContext())
            val alarm = alarmId?.let { db.alarmDao().getAlarmById(it) }

            alarm?.let {
                binding.chooseClock.text = it.time

                val parts = it.time.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hour)
                    .setMinute(minute)
                    .setTitleText("Выберите время для будильника")
                    .build()

                selectedRingtoneUri = it.ringtoneUri

                val savedDays = it.daysOfWeek.split(",")
                selectedDays.addAll(savedDays)

                val dayViews = listOf(
                    binding.textMon to "Mon",
                    binding.textTue to "Tue",
                    binding.textWed to "Wed",
                    binding.textThu to "Thu",
                    binding.textFri to "Fri",
                    binding.textSat to "Sat",
                    binding.textSun to "Sun"
                )
                for ((view, code) in dayViews) {
                    view.alpha = if (code in selectedDays) 1.0f else 0.5f
                }

                binding.spinnerDifficulty.setSelection(it.difficultyLevel - 1)
            }
        }


        // Обработчик нажатия на кнопку выбора времени
        binding.chooseClock.setOnClickListener {
            // Создание диалога выбора времени
            picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H) // 24-часовой формат
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время для будильника")
                .build()

            // Обработка выбора времени
            picker.addOnPositiveButtonClickListener {
                val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.chooseClock.text = formattedTime // Отображаем выбранное время на кнопке
            }

            picker.show(parentFragmentManager, "tag_picker")

        }

        // Обработка выбора дней недели (подсвечивание и сохранение)
        val dayViews = listOf(
            binding.textMon to "Mon",
            binding.textTue to "Tue",
            binding.textWed to "Wed",
            binding.textThu to "Thu",
            binding.textFri to "Fri",
            binding.textSat to "Sat",
            binding.textSun to "Sun"
        )

        for ((textView, dayCode) in dayViews) {
            textView.setOnClickListener {
                if (selectedDays.contains(dayCode)) {
                    selectedDays.remove(dayCode)
                    textView.alpha = 0.5f // Неактивный день
                } else {
                    selectedDays.add(dayCode)
                    textView.alpha = 1.0f // Активный день
                }
            }
        }

        // Обработчик кнопки выбора мелодии
        binding.buttonChooseRingtone.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(intent, RINGTONE_REQUEST_CODE)
        }

        // Обработчик кнопки "Сохранить будильник"
        binding.buttonSaveAlarm.setOnClickListener {
            var selectedTime = binding.chooseClock.text.toString()

            if (!selectedTime.contains(":") || !::picker.isInitialized) {
                selectedTime = "07:00"
                binding.chooseClock.text = selectedTime
                //Toast.makeText(requireContext(), "Пожалуйста, выберите время", Toast.LENGTH_SHORT).show()
                //return@setOnClickListener
            }

            // Проверка разрешения на точные будильники (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:" + requireContext().packageName)
                    }
                    startActivity(intent)
                    Toast.makeText(requireContext(), "Пожалуйста, разрешите установку точных будильников", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            // Проверка оптимизации батареи (Android 6+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val packageName = requireContext().packageName

                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Отключение оптимизации батареи")
                        .setMessage("Чтобы будильник сработал точно, даже в фоновом режиме, необходимо отключить оптимизацию батареи.")
                        .setPositiveButton("Открыть настройки") { _, _ ->
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Не удалось открыть настройки", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Отмена", null)
                        .show()

                    return@setOnClickListener // Не продолжаем, пока не отключена оптимизация
                }
            }

            // Установка самого будильника
            val timeText = binding.chooseClock.text.toString()
            val parts = timeText.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            scheduleAlarm(requireContext(), hour, minute)

            // Получаем уровень сложности из спиннера
            val difficultyLevel = binding.spinnerDifficulty.selectedItemPosition + 1

            // Собираем все данные и передаём их обратно через FragmentResult
            val result = Bundle().apply {
                putString("selected_time", selectedTime)
                putString("selected_days", selectedDays.joinToString(","))
                putString("selected_ringtone", selectedRingtoneUri ?: "")
                putInt("selected_difficulty", difficultyLevel)
            }

            parentFragmentManager.setFragmentResult("alarm_time_key", result)
            parentFragmentManager.popBackStack() // Возврат назад
        }
    }

    // Обработка результата выбора мелодии
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RINGTONE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedRingtoneUri = uri.toString()
                Toast.makeText(requireContext(), "Выбрана мелодия", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Метод установки будильника в AlarmManager
    private fun scheduleAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = System.currentTimeMillis().toInt() // Уникальный ID будильника

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("selected_ringtone", selectedRingtoneUri) // Передаём выбранный рингтон
            action = "ALARM_ACTION_$requestCode"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем время срабатывания будильника
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1) // Если время уже прошло — на следующий день
            }
        }

        Log.d("AlarmDebug", "Будильник запланирован на: ${calendar.time}")

        // Установка точного срабатывания, даже в режиме сна
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    // Освобождаем ресурсы ViewBinding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RINGTONE_REQUEST_CODE = 101 // Код для обработки результата выбора рингтона
    }
}