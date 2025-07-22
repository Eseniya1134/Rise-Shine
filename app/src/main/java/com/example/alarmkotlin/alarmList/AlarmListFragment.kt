package com.example.alarmkotlin.alarmList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmkotlin.R
import com.example.alarmkotlin.alarmList.data.AlarmDatabase
import com.example.alarmkotlin.alarmList.data.AlarmItem
import com.example.alarmkotlin.databinding.FragmentAlarmListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                db.alarmDao().update(updatedAlarm)
                loadAlarms() // обновляем список
            }
        }

        // Настраиваем RecyclerView
        binding.recyclerViewAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAlarms.adapter = adapter

        // Слушаем результат от AddItemAlarmFragment
        parentFragmentManager.setFragmentResultListener("alarm_time_key", viewLifecycleOwner) { _, bundle ->
            val time = bundle.getString("selected_time") ?: return@setFragmentResultListener
            val newAlarm = AlarmItem(time = time)

            // Сохраняем в базу данных
            lifecycleScope.launch {
                db.alarmDao().insert(newAlarm)
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
                db.alarmDao().getAll()
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
