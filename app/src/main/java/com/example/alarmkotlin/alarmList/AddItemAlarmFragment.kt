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

        binding.chooseClock.setOnClickListener {
            val materialTimePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время для будильника")
                .build()

            materialTimePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.MINUTE, materialTimePicker.minute)
                    set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                }

                val formattedTime = String.format("%02d:%02d", materialTimePicker.hour, materialTimePicker.minute)
                binding.chooseClock.text = formattedTime

                // ⬇️ Отправляем результат во фрагмент-список
                val result = Bundle().apply {
                    putString("selected_time", formattedTime)
                }
                parentFragmentManager.setFragmentResult("alarm_time_key", result)
                parentFragmentManager.popBackStack()
            }

            materialTimePicker.show(parentFragmentManager, "tag_picker")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
