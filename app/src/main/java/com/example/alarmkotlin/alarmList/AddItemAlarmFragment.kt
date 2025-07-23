package com.example.alarmkotlin.alarmList

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alarmkotlin.databinding.FragmentAddItemAlarmBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class AddItemAlarmFragment : Fragment() {

    // ViewBinding для доступа к элементам layout
    private var _binding: FragmentAddItemAlarmBinding? = null
    private val binding get() = _binding!!

    // Храним выбранные дни, мелодию и уровень сложности
    private val selectedDays = mutableSetOf<String>()
    private var selectedRingtoneUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка клика по "часы"
        binding.chooseClock.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
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
                    textView.alpha = 0.5f // визуально отключён
                } else {
                    selectedDays.add(dayCode)
                    textView.alpha = 1.0f // визуально включён
                }
            }
        }

        // Обработка кнопки выбора мелодии
        binding.buttonChooseRingtone.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(intent, RINGTONE_REQUEST_CODE)
        }

        // Обработка кнопки сохранения
        binding.buttonSaveAlarm.setOnClickListener {
            val selectedTime = binding.chooseClock.text.toString()
            if (!selectedTime.contains(":")) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите время", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Получаем уровень сложности из Spinner
            val difficultyLevel = binding.spinnerDifficulty.selectedItemPosition + 1 // 1-based

            // Собираем результат
            val result = Bundle().apply {
                putString("selected_time", selectedTime)
                putString("selected_days", selectedDays.joinToString(",")) // например: "Mon,Tue,Wed"
                putString("selected_ringtone", selectedRingtoneUri)
                putInt("selected_difficulty", difficultyLevel)
            }

            // Передаём результат обратно в AlarmListFragment
            parentFragmentManager.setFragmentResult("alarm_time_key", result)

            // Возвращаемся назад
            parentFragmentManager.popBackStack()
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

    // Очищаем binding при уничтожении view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RINGTONE_REQUEST_CODE = 101
    }
}
