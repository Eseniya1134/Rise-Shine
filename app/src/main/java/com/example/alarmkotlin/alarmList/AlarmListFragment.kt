package com.example.alarmkotlin.alarmList

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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

    private var actionMode: ActionMode? = null // панель сверху

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

        adapter = AlarmAdapter(
            fragmentManager = parentFragmentManager,
            alarms = listOf(), // или твой список
            onItemLongClick = { alarm ->
                if (actionMode == null) {
                    actionMode = requireActivity().startActionMode(actionModeCallback)
                }
                adapter.toggleSelection(alarm)
            },
            onItemClick = { alarm ->
                if (adapter.isSelectionMode) {
                    adapter.toggleSelection(alarm)
                    if (adapter.getSelectedItems().isEmpty()) {
                        actionMode?.finish()
                    }
                } else {
                    val fragment = AddItemAlarmFragment().apply {
                        arguments = Bundle().apply {
                            putInt("alarm_id", alarm.id)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameAlarm, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            },
            onToggle = { updatedAlarm ->
                lifecycleScope.launch {
                    db.alarmDao().updateAlarm(updatedAlarm)
                    loadAlarms()
                }
            }
        )



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

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_selection, menu)



            adapter.isSelectionMode = true
            return true
        }



        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_delete -> {
                    // Удаление выбранных будильников
                    val selected = adapter.getSelectedItems()
                    lifecycleScope.launch {
                        selected.forEach { alarm ->
                            db.alarmDao().deleteAlarm(alarm)
                        }
                        loadAlarms() // обновить список
                        mode?.finish() // закрыть ActionMode
                    }
                    return true
                }

                R.id.action_cancel -> {
                    mode?.finish() // отменить выбор
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            adapter.isSelectionMode = false
            actionMode = null
        }
    }



    // Очищаем binding, чтобы избежать утечек памяти
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
