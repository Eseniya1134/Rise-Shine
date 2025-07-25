package com.example.alarmkotlin.alarmList

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmkotlin.AlarmActivity
import com.example.alarmkotlin.R
import com.example.alarmkotlin.alarmList.data.AlarmDatabase
import com.example.alarmkotlin.alarmList.data.AlarmItem
import com.example.alarmkotlin.databinding.FragmentAlarmListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlarmListFragment : Fragment() {

    // ViewBinding — безопасный доступ к элементам интерфейса
    private var _binding: FragmentAlarmListBinding? = null
    private val binding get() = _binding!!

    // Адаптер для отображения списка будильников
    private lateinit var adapter: AlarmAdapter
    private lateinit var db: AlarmDatabase // База данных

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем доступ к базе данных
        db = AlarmDatabase.getDatabase(requireContext())

        // Создаём адаптер и передаём в него callback при переключении будильника
        adapter = AlarmAdapter(emptyList()) { updatedAlarm ->
            lifecycleScope.launch {
                db.alarmDao().updateAlarm(updatedAlarm)
                loadAlarms() // обновляем список
            }
        }

        // Настраиваем RecyclerView
        binding.recyclerViewAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAlarms.adapter = adapter

        // Слушаем результат от AddItemAlarmFragment
        parentFragmentManager.setFragmentResultListener("alarm_time_key", viewLifecycleOwner) { _, bundle ->
            val time = bundle.getString("selected_time") ?: return@setFragmentResultListener

            // Получаем дополнительные параметры из AddItemAlarmFragment
            val daysOfWeek = bundle.getString("selected_days") ?: "Mon,Tue" // Дни недели по умолчанию
            val ringtone = bundle.getString("selected_ringtone") // Путь к мелодии (может быть null)
            val difficulty = bundle.getInt("selected_difficulty", 1) // Уровень сложности (по умолчанию — 1)

            // Создаём новый объект будильника с учётом всех полей
            val newAlarm = AlarmItem(
                time = time,
                daysOfWeek = daysOfWeek,
                ringtoneUri = ringtone,
                difficultyLevel = difficulty
            )

            // Сохраняем в базу данных
            lifecycleScope.launch {
                db.alarmDao().insertAlarm(newAlarm)
                loadAlarms()
            }
        }

        // Кнопка для перехода на экран добавления нового будильника
        binding.buttonAddAlarm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameAlarm, AddItemAlarmFragment()) // заменяем текущий фрагмент
                .addToBackStack(null) // можно вернуться назад
                .commit()
        }

        loadAlarms() // загружаем список будильников при старте
    }

    // Загружает будильники из БД
    private fun loadAlarms() {
        lifecycleScope.launch {
            val alarms = withContext(Dispatchers.IO) {
                db.alarmDao().getAllAlarms()
            }
            adapter.updateList(alarms)
        }
    }




    // Очищаем binding, чтобы избежать утечек памяти
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
