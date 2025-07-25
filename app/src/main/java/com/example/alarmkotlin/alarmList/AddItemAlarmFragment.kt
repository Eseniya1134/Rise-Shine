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
import com.example.alarmkotlin.AlarmActivity
import com.example.alarmkotlin.MainActivity
import com.example.alarmkotlin.databinding.FragmentAddItemAlarmBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class AddItemAlarmFragment : Fragment() {

    private var _binding: FragmentAddItemAlarmBinding? = null
    private val binding get() = _binding!!

    private val selectedDays = mutableSetOf<String>()
    private var selectedRingtoneUri: String? = null

    // Сделаем picker полем класса, чтобы доступ был из любого места
    private lateinit var picker: MaterialTimePicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация picker один раз, но лучше создавать при нажатии (как раньше)
        // Но чтобы использовать picker.hour и picker.minute в save, лучше создать при клике и сохранить в поле

        binding.chooseClock.setOnClickListener {
            picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время для будильника")
                .build()

            picker.addOnPositiveButtonClickListener {
                val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.chooseClock.text = formattedTime
            }

            picker.show(parentFragmentManager, "tag_picker")
        }

        // Обработка кликов по дням недели
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
                    textView.alpha = 0.5f
                } else {
                    selectedDays.add(dayCode)
                    textView.alpha = 1.0f
                }
            }
        }

        binding.buttonChooseRingtone.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(intent, RINGTONE_REQUEST_CODE)
        }


        binding.buttonSaveAlarm.setOnClickListener {
            val selectedTime = binding.chooseClock.text.toString()
            if (!selectedTime.contains(":")) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите время", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!::picker.isInitialized) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите время", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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

            // Проверка оптимизации батареи
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

                    return@setOnClickListener // Не сохраняем будильник, пока не отключили оптимизацию
                }
            }

            // Установка будильника
            val hour = picker.hour
            val minute = picker.minute
            scheduleAlarm(requireContext(), hour, minute)

            val difficultyLevel = binding.spinnerDifficulty.selectedItemPosition + 1

            val result = Bundle().apply {
                putString("selected_time", selectedTime)
                putString("selected_days", selectedDays.joinToString(","))
                putString("selected_ringtone", selectedRingtoneUri ?: "")
                putInt("selected_difficulty", difficultyLevel)
            }

            parentFragmentManager.setFragmentResult("alarm_time_key", result)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RINGTONE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedRingtoneUri = uri.toString()
                Toast.makeText(requireContext(), "Выбрана мелодия", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = System.currentTimeMillis().toInt() // Уникальный ID

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("selected_ringtone", selectedRingtoneUri)
            action = "ALARM_ACTION_$requestCode"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        Log.d("AlarmDebug", "Будильник запланирован на: ${calendar.time}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RINGTONE_REQUEST_CODE = 101
    }

}