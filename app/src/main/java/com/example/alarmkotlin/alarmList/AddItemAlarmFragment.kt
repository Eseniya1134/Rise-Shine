package com.example.alarmkotlin.alarmList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.alarmkotlin.databinding.FragmentAddItemAlarmBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class AddItemAlarmFragment : Fragment() {

    // ViewBinding для доступа к элементам layout
    private var _binding: FragmentAddItemAlarmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка клика по кнопке "Выбрать время"
        binding.chooseClock.setOnClickListener {

            // Инициализируем таймпикер (24ч формат, 12:00 по умолчанию)
            val materialTimePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время для будильника")
                .build()

            // Когда пользователь нажмёт "ОК"
            materialTimePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.MINUTE, materialTimePicker.minute)
                    set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                }

                // Форматируем выбранное время как строку
                val formattedTime = String.format("%02d:%02d", materialTimePicker.hour, materialTimePicker.minute)

                // Отображаем время в кнопке
                binding.chooseClock.text = formattedTime

                // Передаём результат в AlarmListFragment через FragmentResult API
                val result = Bundle().apply {
                    putString("selected_time", formattedTime)
                }
                parentFragmentManager.setFragmentResult("alarm_time_key", result)

                // Возвращаемся назад к списку
                parentFragmentManager.popBackStack()
            }

            // Показываем диалог выбора времени
            materialTimePicker.show(parentFragmentManager, "tag_picker")
        }
    }

    // Очищаем binding при уничтожении view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
