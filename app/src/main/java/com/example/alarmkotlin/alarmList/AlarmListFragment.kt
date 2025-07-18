package com.example.alarmkotlin.alarmList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmkotlin.alarmList.data.AlarmDatabase
import com.example.alarmkotlin.alarmList.data.AlarmItem
import com.example.alarmkotlin.databinding.FragmentAlarmListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmListFragment : Fragment() {

    private var _binding: FragmentAlarmListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AlarmAdapter
    private lateinit var db: AlarmDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AlarmDatabase.getDatabase(requireContext())

        adapter = AlarmAdapter(emptyList()) { updatedAlarm ->
            lifecycleScope.launch {
                db.alarmDao().update(updatedAlarm)
                loadAlarms()
            }
        }

        binding.recyclerViewAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAlarms.adapter = adapter

        binding.buttonAddAlarm.setOnClickListener {
            val newAlarm = AlarmItem(time = "07:00") // временное значение
            lifecycleScope.launch {
                db.alarmDao().insert(newAlarm)
                loadAlarms()
            }
        }

        loadAlarms()
    }

    private fun loadAlarms() {
        lifecycleScope.launch {
            val alarms = withContext(Dispatchers.IO) {
                db.alarmDao().getAll()
            }
            adapter.updateList(alarms)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * - _binding — переменная, в которую мы сохраняем ссылку на ViewBinding фрагмента.
 * - binding — это обёртка, которая позволяет обращаться к _binding как к non-null
 * - ViewBinding создаётся в onCreateView() и уничтожается в onDestroyView().
 * - Мы обнуляем _binding = null в onDestroyView(), чтобы избежать утечек памяти.
 * - Такой подход — стандарт для работы с ViewBinding во фрагментах.

Объяснение:
- Этот фрагмент отображает список будильников с возможностью добавления и включения/отключения.
- Используется ViewBinding для доступа к элементам макета Fragment.
- Room используется для хранения будильников, а корутины — для работы с БД в фоне.
- loadAlarms загружает актуальный список и передаёт его адаптеру.
*/
