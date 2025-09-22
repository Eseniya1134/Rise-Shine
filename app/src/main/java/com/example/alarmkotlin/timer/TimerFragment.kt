package com.example.alarmkotlin.timer

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.alarmkotlin.R
import com.example.alarmkotlin.databinding.FragmentAddItemAlarmBinding
import com.example.alarmkotlin.databinding.FragmentTimerBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


class TimerFragment : Fragment() {

    // Таймпикер для выбора времени
    private lateinit var picker: MaterialTimePicker
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.chooseTimer.setOnClickListener {
            initTimePicker()  // Обработчик нажатия на кнопку выбора времени
        }


        /* Обработчик кнопки "Сохранить будильник"
        binding.buttonSaveAlarm.setOnClickListener {
            var selectedTime = binding.chooseClock.text.toString()

            if (!selectedTime.contains(":") || !::picker.isInitialized) {
                binding.chooseClock.text = "07:00"
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


            // Получаем уровень сложности из спиннера
            val difficultyLevel = binding.spinnerDifficulty.selectedItemPosition + 1

            if (alarmId != null){
                scheduleAlarm(requireContext(), hour, minute, difficultyLevel, alarmId!!, selectedDays.joinToString(","), timeText)
            }else{
                val newId = generId()
                scheduleAlarm(requireContext(), hour, minute, difficultyLevel, newId, selectedDays.joinToString(","), timeText)
            }


            // Собираем все данные и передаём их обратно через FragmentResult
            val result = Bundle().apply {
                putString("selected_time", selectedTime)
                putString("selected_days", selectedDays.joinToString(","))
                putString("selected_ringtone", selectedRingtoneUri ?: "")
                putInt("selected_difficulty", difficultyLevel)
            }

            parentFragmentManager.setFragmentResult("alarm_time_key", result)
            parentFragmentManager.popBackStack() // Возврат назад
        }*/
    }

    private fun initTimePicker() {
        val dialogView = layoutInflater.inflate(R.layout.timer_dialog, null)

        AlertDialog.Builder(requireContext())
            .setTitle("Установить таймер")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val minutes = dialogView.findViewById<EditText>(R.id.editMinutes).text.toString()
                    .toIntOrNull() ?: 0
                val seconds = dialogView.findViewById<EditText>(R.id.editSeconds).text.toString()
                    .toIntOrNull() ?: 0

                val totalMillis = (minutes * 60 + seconds) * 1000L
                Toast.makeText(
                    requireContext(),
                    "Таймер: $minutes мин $seconds сек",
                    Toast.LENGTH_SHORT
                ).show()

                // тут можешь запустить CountDownTimer с totalMillis
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}