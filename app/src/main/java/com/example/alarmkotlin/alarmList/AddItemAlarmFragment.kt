package com.example.alarmkotlin.alarmList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmkotlin.R
import com.example.alarmkotlin.alarmList.data.AlarmDatabase
import com.example.alarmkotlin.alarmList.data.AlarmItem
import com.example.alarmkotlin.databinding.FragmentAddItemAlarmBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class AddItemAlarmFragment : Fragment() {

    private var _binding: FragmentAddItemAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AlarmAdapter
    private lateinit var db: AlarmDatabase

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

                //val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                /*val alarmClockInfo = AlarmManager.AlarmClockInfo(
                    calendar.timeInMillis,
                    getAlarmInfoPendingIntent()
                )

                alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent())

                Toast.makeText(
                    this,
                    "Будильник установлен на ${sdf.format(calendar.time)}",
                    Toast.LENGTH_SHORT
                ).show()*/
            }

            materialTimePicker.show(parentFragmentManager, "tag_picker")
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}